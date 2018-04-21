package org.davidmoten.util.io;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

public class IOUtilTest {

    @Test
    public void test() throws IOException {
        String s = "hi there";
        ByteArrayInputStream a = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        InputStream b = IOUtil.pipe(a, o -> new GZIPOutputStream(o));
        List<String> list = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(b), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.toList());
        assertEquals("hi there", list.get(0));
        assertEquals(1, list.size());
    }

}
