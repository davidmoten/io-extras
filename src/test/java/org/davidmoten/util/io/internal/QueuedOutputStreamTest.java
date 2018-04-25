package org.davidmoten.util.io.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

public class QueuedOutputStreamTest {

    @Test
    public void testIsCopy() throws IOException {
        Queue<ByteBuffer> queue = new LinkedList<>();
        QueuedOutputStream q = new QueuedOutputStream(queue, new int[1]);
        byte[] b = new byte[] { 100, 101 };
        q.write(b);
        ByteBuffer bb = queue.poll();
        assertEquals(2, bb.remaining());
        assertEquals(0, bb.position());
        assertEquals(2, bb.limit());
        assertArrayEquals(b, bb.array());
        assertNotEquals(b, bb.array());
        // get coverage of flush which does nothing
        q.flush();
    }

}
