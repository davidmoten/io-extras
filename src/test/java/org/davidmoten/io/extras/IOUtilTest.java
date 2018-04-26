package org.davidmoten.io.extras;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.davidmoten.io.extras.IOUtil;
import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class IOUtilTest {

    @Test
    public void testRoundTripIdentity() throws IOException {
        testRoundTripIdentity("hi there", 128, 128);
    }

    @Test
    public void testRoundTripIdentityWithSmallBuffers() throws IOException {
        testRoundTripIdentity("hi there", 2, 3);
    }

    private void testRoundTripIdentity(String s, int bufferSizeLeft, int bufferSizeRight)
            throws IOException {
        InputStream a = new BufferedInputStream(
                new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), bufferSizeLeft);
        InputStream b = IOUtil.pipe(a, o -> o, bufferSizeRight);
        List<String> list = new BufferedReader(new InputStreamReader(b, StandardCharsets.UTF_8))
                .lines().collect(Collectors.toList());
        assertEquals(s, list.get(0));
        assertEquals(1, list.size());
    }

    @Test
    public void testRoundTripGzip() throws IOException {
        testRoundTripGzip("hi there", 128);
    }

    @Test
    public void testRoundTripGzipLong() throws IOException {
        testRoundTripGzip(createLongString(), 8192);
    }

    @Test
    public void testWriteInConstructorAndWriteOnClose() throws IOException {
        byte[] m = new byte[] { 101, 102 };
        ByteArrayInputStream a = new ByteArrayInputStream(m);
        InputStream b = IOUtil.pipe(a, o -> new SpecialOutputStream(o));
        assertArrayEquals(new byte[] { 1, 2, 101, 102, 5 }, readAll(b));
    }

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(IOUtil.class);
    }

    private static String createLongString() {
        StringWriter w = new StringWriter();
        for (int i = 0; i < 1000; i++) {
            w.write(UUID.randomUUID().toString());
        }
        return w.toString();
    }

    private void testRoundTripGzip(String s, int bufferSize) throws IOException {
        byte[] m = s.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream g = new GZIPOutputStream(out);
        g.write(m);
        g.close();
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> new GZIPOutputStream(o, 8, false), bufferSize);
        assertArrayEquals(m, readAll(new GZIPInputStream(b)));
    }

    @Test
    public void testGzip() throws IOException {
        byte[] bytes = "hello there you how's things?".getBytes();
        assertArrayEquals(bytes,
                readAll(IOUtil.gunzip(IOUtil.gzip(new ByteArrayInputStream(bytes)))));
    }

    @Test
    public void testMarkNotSupported() throws IOException {
        byte[] bytes = "hello there you how's things?".getBytes();
        InputStream is = IOUtil.gzip(new ByteArrayInputStream(bytes));
        assertFalse(is.markSupported());
    }

    @Test
    public void testMarkDoesNothing() throws IOException {
        byte[] bytes = "hello there you how's things?".getBytes();
        InputStream is = IOUtil.gzip(new ByteArrayInputStream(bytes));
        is.mark(1);
    }

    @Test
    public void testSkip() throws IOException {
        byte[] bytes = new byte[] { -45, 62, 77 };
        {
            InputStream is = IOUtil.gzip(new ByteArrayInputStream(bytes));
            System.out.println(Arrays.toString(readAll(is)));
        }
        InputStream is = IOUtil.gzip(new ByteArrayInputStream(bytes));
        assertEquals(31, is.read());
        assertEquals(9, is.skip(9));
        assertEquals(-69 & 0xff, is.read());
    }

    @Test
    public void testReadOfLength0() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        assertEquals(100, is.read());
        assertEquals(0, is.read(new byte[2], 0, 0));
    }
    
    @Test
    public void testOffsetUsedInRead() throws IOException {
        byte[] bytes = new byte[] { 100, 101};
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> o);
        byte[] buffer = new byte[10];
        is.read(buffer, 7, 2);
        assertEquals(buffer[7], 100);
        assertEquals(buffer[8], 101);
    }
    
    @Test
    public void testAvailable() throws IOException {
        byte[] bytes = new byte[] { 100, 101};
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> o);
        assertEquals(0, is.available());
        is.read();
        assertEquals(1, is.available());
        is.read();
        assertEquals(0, is.available());
        assertEquals(-1, is.read());
        assertEquals(0, is.available());
    }

    @Test
    public void testSkipToEnd() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        assertEquals(2, is.skip(2));
        assertEquals(-1, is.skip(1));
    }

    @Test
    public void testSkipPastEnd() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        assertEquals(2, is.skip(3));
        assertEquals(-1, is.skip(1));
    }

    @Test(expected = IOException.class)
    public void testResetThrows() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        is.reset();
    }

    @Test
    public void testByteByByte() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        assertEquals(100, is.read());
        assertEquals(101, is.read());
        assertEquals(-1, is.read());
    }

    @Test
    public void testReadAfterCloseThrows() throws IOException {
        byte[] bytes = new byte[] { 100, 101 };
        InputStream is = IOUtil.pipe(new ByteArrayInputStream(bytes),
                o -> new ByByteOutputStream(o));
        is.close();
        try {
            is.read();
            fail();
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }
    }

    @Test
    public void testRoundTripGzipBytePerByte() throws IOException {
        String s = "hi there";
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> new GZIPOutputStream(new ByByteOutputStream(o)));
        List<String> list = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(b), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.toList());
        assertEquals("hi there", list.get(0));
        assertEquals(1, list.size());
    }

    private static byte[] readAll(InputStream is) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        try {
            while ((n = is.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
            }
            bytes.close();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class ByByteOutputStream extends OutputStream {

        private final OutputStream os;

        ByByteOutputStream(OutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }

    }

    private static final class SpecialOutputStream extends OutputStream {

        private final OutputStream os;

        SpecialOutputStream(OutputStream os) throws IOException {
            this.os = os;
            os.write(create(2));
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }

        @Override
        public void close() throws IOException {
            os.write((byte) 5);
        }

    }

    private static byte[] create(int length) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = (byte) (i + 1);
        }
        return b;
    }

}
