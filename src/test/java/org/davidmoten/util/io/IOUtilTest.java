package org.davidmoten.util.io;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
        testRoundTripIdentity("hi there");
    }

    private void testRoundTripIdentity(String s) throws IOException {
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> o);
        List<String> list = new BufferedReader(new InputStreamReader(b, StandardCharsets.UTF_8))
                .lines().collect(Collectors.toList());
        assertEquals(s, list.get(0));
        assertEquals(1, list.size());
    }

    @Test
    public void testRoundTripGzip() throws IOException {
        testRoundTripGzip("hi there");
    }

    @Test
    public void testRoundTripGzipLong() throws IOException {
        testRoundTripGzip(createLongString());
    }

    private static String createLongString() {
        StringWriter w = new StringWriter();
        for (int i = 0; i < 1000; i++) {
            w.write(UUID.randomUUID().toString());
        }
        return w.toString();
    }

    private void testRoundTripGzip(String s) throws IOException {
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> new GZIPOutputStream(o));
        List<String> list = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(b), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.toList());
        assertEquals(s, list.get(0));
        assertEquals(1, list.size());
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
