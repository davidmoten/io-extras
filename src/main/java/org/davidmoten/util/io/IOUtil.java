package org.davidmoten.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.davidmoten.util.io.internal.TransformedInputStream;

public final class IOUtil {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private IOUtil() {
        // prevent instantiation
    }

    public static InputStream pipe(InputStream is,
            IOFunction<? super OutputStream, ? extends OutputStream> transform,
            int bufferSize) throws IOException {
        return new TransformedInputStream(is, transform, bufferSize);
    }

    public static InputStream pipe(InputStream is,
            IOFunction<? super OutputStream, ? extends OutputStream> transform)
            throws IOException {
        return pipe(is, transform, DEFAULT_BUFFER_SIZE);
    }

}