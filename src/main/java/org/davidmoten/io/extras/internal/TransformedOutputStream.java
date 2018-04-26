package org.davidmoten.io.extras.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.davidmoten.io.extras.IOFunction;

public final class TransformedOutputStream extends OutputStream {

    private final byte[] singleByte = new byte[1];
    private final QueuedInputStream qis;
    private final InputStream is;

    public TransformedOutputStream(OutputStream os,
            IOFunction<? super InputStream, ? extends InputStream> transform, int bufferSize)
            throws IOException {
        qis = new QueuedInputStream();
        is = transform.apply(qis);
    }

    @Override
    public void write(int b) throws IOException {
        singleByte[0] = (byte) b;
        write(singleByte, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(b, off, len);
        qis.add(Util.copy(bb));
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    @Override
    public void close() throws IOException {
        // TODO
    }

}
