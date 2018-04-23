package org.davidmoten.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public final class IOUtil {

    private IOUtil() {
        // prevent instantiation
    }

    public static InputStream pipe(InputStream is,
            FunctionCanThrow<? super OutputStream, ? extends OutputStream> transform,
            int bufferSize) throws IOException {
        return new TransformedInputStream(is, transform, bufferSize);
    }

    public static InputStream pipe(InputStream is,
            FunctionCanThrow<? super OutputStream, ? extends OutputStream> FunctionCanThrow)
            throws IOException {
        return pipe(is, FunctionCanThrow, 8192);
    }

    private static final class TransformedInputStream extends InputStream implements Runnable {

        private final InputStream is;
        private final Deque<ByteBuffer> queue;
        private final int bufferSize;
        private final OutputStream out;
        private boolean done;
        private boolean closed;

        TransformedInputStream(InputStream is,
                FunctionCanThrow<? super OutputStream, ? extends OutputStream> transform,
                int bufferSize) throws IOException {
            this.is = is;
            this.queue = new ArrayDeque<>();
            this.out = transform.apply(new QueuedOutputStream(queue));
            this.bufferSize = bufferSize;
        }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int n = readInternal(b, 0, 1);
            if (n == -1) {
                return -1;
            } else {
                return b[0] & 0xff; // must be 0-255
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
                        byte[] c = new byte[bufferSize];
                        int n = is.read(c);
                        if (n == -1) {
                            done = true;
                            out.close();
                        } else if (n > 0) {
                            out.write(c, 0, n);
                        } else {
                            return 0;
                        }
                    }
                } else {
                    int n = Math.min(bb.remaining(), length);
                    if (bytes != null) {
                        bb.get(bytes, 0, n);
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
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void reset() throws IOException {
            is.reset();
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

    private static final class QueuedOutputStream extends OutputStream {

        private final Queue<ByteBuffer> queue;

        QueuedOutputStream(Queue<ByteBuffer> queue) {
            this.queue = queue;
        }

        @Override
        public void write(byte[] b) throws IOException {
            queue.add(ByteBuffer.wrap(b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            queue.add(ByteBuffer.wrap(b, off, len));
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

        @Override
        public void write(int b) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(1);
            bb.put((byte) (b));
            bb.position(0);
            queue.add(bb);
        }

    }
}
