package org.davidmoten.io.extras;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.davidmoten.io.extras.internal.TransformedInputStream;
import org.davidmoten.io.extras.internal.TransformedOutputStream;

public final class IOUtil {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private IOUtil() {
        // prevent instantiation
    }

    public static InputStream pipe(InputStream is, IOFunction<? super OutputStream, ? extends OutputStream> transform,
            int bufferSize) throws IOException {
        return new TransformedInputStream(is, transform, bufferSize);
    }

    public static InputStream pipe(InputStream is, IOFunction<? super OutputStream, ? extends OutputStream> transform)
            throws IOException {
        return pipe(is, transform, DEFAULT_BUFFER_SIZE);
    }

    public static InputStream gzip(InputStream is) throws IOException {
        return pipe(is, o -> new GZIPOutputStream(o));
    }

    public static InputStream gunzip(InputStream is) throws IOException {
        return new GZIPInputStream(is);
    }

    public static OutputStream pipe(OutputStream os, IOFunction<? super InputStream, ? extends InputStream> transform)
            throws IOException {
        return pipe(os, transform, DEFAULT_BUFFER_SIZE);
    }

    private static OutputStream pipe(OutputStream os,
            IOFunction<? super InputStream, ? extends InputStream> transform,
            int bufferSize) throws IOException {
        return new TransformedOutputStream(os, transform, bufferSize);
    }
    
}