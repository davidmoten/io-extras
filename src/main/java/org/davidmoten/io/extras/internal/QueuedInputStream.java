package org.davidmoten.io.extras.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public final class QueuedInputStream extends InputStream {

    private final byte[] singleByte = new byte[1];
    private final Deque<ByteBuffer> queue = new ArrayDeque<>();
    private boolean closed;
    private CountDownLatch latch = new CountDownLatch(1);
    private final AtomicInteger count = new AtomicInteger();
    private final ReentrantLock lock = new ReentrantLock();

    public QueuedInputStream() {
        lock.lock();
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
        if (len == 0) {
            return 0;
        }
        try {
            lock.tryLock(Long.MAX_VALUE, TimeUnit.DAYS);
            ByteBuffer bb = queue.poll();
            int n = Math.min(bb.remaining(), len);
            bb.get(b, off, n);
            latch = new CountDownLatch(1);
            if (bb.remaining() > 0) {
                add(bb);
            }
            return n;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public void add(ByteBuffer bb) {
        queue.offer(bb);
        lock.unlock();
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

}
