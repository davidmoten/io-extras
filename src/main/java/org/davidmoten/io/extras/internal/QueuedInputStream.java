package org.davidmoten.io.extras.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public final class QueuedInputStream extends InputStream {

    private final byte[] singleByte = new byte[1];
    private final Deque<ByteBuffer> queue = new ArrayDeque<>();
    private boolean closed;

    public QueuedInputStream() {
    }

    @Override
    public int read() throws IOException {
        return read(singleByte, 0, 1);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // TODO Auto-generated method stub
        return super.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        // TODO Auto-generated method stub
        return super.skip(n);
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public void mark(int readlimit) {
        // do nothing
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    public void add(ByteBuffer bb) {
        queue.offer(bb);
    }

}
