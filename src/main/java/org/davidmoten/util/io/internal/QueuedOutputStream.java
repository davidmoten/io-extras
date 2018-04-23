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
