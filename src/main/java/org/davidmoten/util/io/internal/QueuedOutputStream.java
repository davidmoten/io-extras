package org.davidmoten.util.io.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

public final class QueuedOutputStream extends OutputStream {

    private final Queue<ByteBuffer> queue;

    QueuedOutputStream(Queue<ByteBuffer> queue) {
        this.queue = queue;
    }

    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) b;
        queue.add(ByteBuffer.wrap(bytes));
    }

    @Override
    public void write(byte[] b) throws IOException {
        add(ByteBuffer.wrap(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        add(ByteBuffer.wrap(b, off, len));
    }

    private void add(ByteBuffer bb) {
        queue.add(bb);
        System.out.println("added to queue " + bb);
    }

    @Override
    public void flush() throws IOException {
        // ignore
    }

    @Override
    public void close() throws IOException {
        System.out.println("closed");
        // ignore
    }

}
