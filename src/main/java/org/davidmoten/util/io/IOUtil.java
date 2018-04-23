package org.davidmoten.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.davidmoten.util.io.internal.TransformedInputStream;

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

}