package org.davidmoten.util.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

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

    private static String createLongString() {
        StringWriter w = new StringWriter();
        for (int i = 0; i < 30; i++) {
            w.write(UUID.randomUUID().toString());
        }
        return w.toString();
    }

    private void testRoundTripGzip(String s, int bufferSize) throws IOException {
        byte[] m = s.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> new GZIPOutputStream(o), bufferSize);
        assertArrayEquals(m, readAll(new GZIPInputStream(b)));
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

}
