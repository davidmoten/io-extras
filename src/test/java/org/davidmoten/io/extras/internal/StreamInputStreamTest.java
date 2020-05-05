package org.davidmoten.io.extras.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.davidmoten.io.extras.IOUtil;
import org.junit.Test;

public class StreamInputStreamTest {
    
    @Test
    public void testReadByte() throws IOException {
        InputStream in = IOUtil.toInputStream(Stream.of(new byte[] {1, 2}, new byte[] {3, 4}));
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
        assertEquals(4, in.read());
        assertEquals(-1, in.read());
    }
    
    @Test
    public void testReadBytes() throws IOException {
        InputStream in = IOUtil.toInputStream(Stream.of(new byte[] {1, 2}, new byte[] {3, 4}));
        byte[] bytes = new byte[1]; 
        assertEquals(1, in.read(bytes));
        assertEquals(1, bytes[0]);
        assertEquals(1, in.read(bytes));
        assertEquals(2, bytes[0]);
        assertEquals(1, in.read(bytes));
        assertEquals(3, bytes[0]);
        assertEquals(1, in.read(bytes));
        assertEquals(4, bytes[0]);
        assertEquals(-1, in.read(bytes));
    }

    @Test
    public void testReadBytesInChunks() throws IOException {
        InputStream in = IOUtil.toInputStream(Stream.of(new byte[] {1, 2}, new byte[] {3, 4}));
        byte[] bytes = new byte[3]; 
        assertEquals(3, in.read(bytes));
        assertEquals(1, bytes[0]);
        assertEquals(2, bytes[1]);
        assertEquals(3, bytes[2]);
        assertEquals(1, in.read(bytes));
        assertEquals(4, bytes[0]);
        assertEquals(-1, in.read(bytes));
    }
    
    @Test
    public void testReadBytesAll() throws IOException {
        InputStream in = IOUtil.toInputStream(Stream.of(new byte[] {1, 2}, new byte[] {3, 4}));
        byte[] bytes = new byte[10]; 
        assertEquals(4, in.read(bytes));
        assertEquals(1, bytes[0]);
        assertEquals(2, bytes[1]);
        assertEquals(3, bytes[2]);
        assertEquals(4, bytes[3]);
        assertEquals(-1, in.read(bytes));
    }

    @Test
    public void testReadBytesAllOffset() throws IOException {
        InputStream in = IOUtil.toInputStream(Stream.of(new byte[] {1, 2}, new byte[] {3, 4}));
        byte[] bytes = new byte[10]; 
        assertEquals(4, in.read(bytes, 2, 8));
        assertEquals(1, bytes[2]);
        assertEquals(2, bytes[3]);
        assertEquals(3, bytes[4]);
        assertEquals(4, bytes[5]);
        assertEquals(-1, in.read(bytes));
    }
    
}
