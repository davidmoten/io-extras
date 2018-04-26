package org.davidmoten.io.extras.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

public class QueuedOutputStreamTest {

    @Test
    public void testIsCopy() throws IOException {
        Queue<ByteBuffer> queue = new LinkedList<>();
        try (QueuedOutputStream q = new QueuedOutputStream(queue, new int[1])) {
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

    @Test
    public void writeAfterCloseThrows() throws IOException {
        Queue<ByteBuffer> queue = new LinkedList<>();
        try (QueuedOutputStream q = new QueuedOutputStream(queue, new int[1])) {
            q.close();
            try {
                q.write(100);
                fail();
            } catch (IOException e) {
                assertEquals("Stream closed", e.getMessage());
            }
        }
    }

}
