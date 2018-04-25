package org.davidmoten.util.io.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.davidmoten.util.io.IOFunction;

public final class TransformedInputStream extends InputStream implements Runnable {

    private final InputStream is;
    private final Deque<ByteBuffer> queue;
    private final int bufferSize;
    private final OutputStream out;
    private final byte[] singleByte = new byte[1];
    private final byte[] buffer;
    private boolean done;
    private boolean closed;

    public TransformedInputStream(InputStream is,
            IOFunction<? super OutputStream, ? extends OutputStream> transform, int bufferSize)
            throws IOException {
        this.is = is;
        this.queue = new ArrayDeque<>();
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
        this.out = transform.apply(new QueuedOutputStream(queue));
    }

    @Override
    public int read() throws IOException {
        int n = readInternal(singleByte, 0, 1);
        if (n == -1) {
            return -1;
        } else {
            return singleByte[0] & 0xff; // must be 0-255
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return readInternal(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return readInternal(b, off, len);
    }

    private int readInternal(byte[] bytes, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        if (closed) {
            throw new IOException("Stream closed");
        }
        while (true) {
            ByteBuffer bb = queue.poll();
            if (bb == null) {
                if (done) {
                    return -1;
                } else {
                    int n = is.read(buffer);
                    if (n == -1) {
                        done = true;
                        out.close();
                    } else {
                        out.write(buffer, 0, n);
                    }
                }
            } else {
                int n = Math.min(bb.remaining(), length);
                if (bytes != null) {
                    bb.get(bytes, 0, n);
                } else {
                    bb.position(bb.position() + n);
                }
                if (bb.remaining() > 0) {
                    queue.offerLast(bb);
                }
                return n;
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long result = 0;
        while (true) {
            if (n == 0) {
                return result;
            } else {
                int m = Math.min((int) Math.min((long) Integer.MAX_VALUE, n), bufferSize);
                int v = readInternal(null, 0, m);
                if (v != -1) {
                    result += v;
                    n -= v;
                } else {
                    return result;
                }
            }
        }
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        closed = true;
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

    @Override
    public void run() {
        closed = true;
    }

}
