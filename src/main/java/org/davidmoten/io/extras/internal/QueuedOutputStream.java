package org.davidmoten.io.extras.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

public final class QueuedOutputStream extends OutputStream {

    private final Queue<ByteBuffer> queue;
    private final int[] count;// single element array
    private boolean closed;

    QueuedOutputStream(Queue<ByteBuffer> queue, int[] count) {
        this.queue = queue;
        this.count = count;
    }

    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) b;
        add(ByteBuffer.wrap(bytes));
    }

    @Override
    public void write(byte[] b) throws IOException {
        add(ByteBuffer.wrap(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        add(ByteBuffer.wrap(b, off, len));
    }

    private void add(ByteBuffer bb) throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        // must copy the byte buffer because may get reused upstream (this happens with
        // GzipOutputStream!)
        count[0] += bb.remaining();
        queue.offer(Util.copy(bb));
    }

    @Override
    public void flush() throws IOException {
        // ignore
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

}
