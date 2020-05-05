package org.davidmoten.io.extras.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;

public class StreamInputStream extends InputStream {

    private final Stream<byte[]> stream;
    private Iterator<byte[]> it;
    private byte[] current;
    private int index = 0;

    public StreamInputStream(Stream<byte[]> stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        if (isEof()) {
            return -1;
        }
        return current[index++] & 0xff;
    }

    @Override
    public int read(byte[] b, final int off, final int len) throws IOException {
        if (isEof()) {
            return -1;
        }
        int off2 = off;
        int len2 = len;
        while (true) {
            int length = Math.min(current.length - index, len2);
            System.arraycopy(current, index, b, off2, length);
            index+= length;
            off2+= length;
            len2-= length;
            if (isEof() || len2 == 0) {
                break;
            } 
        }
        return len - len2;
    }

    private boolean isEof() {
        if (it == null) {
            it = stream.iterator();
        }
        // need to use a while loop here to handle empty byte arrays in the stream
        while (current == null || index == current.length) {
            if (it.hasNext()) {
                current = it.next();
                index = 0;
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
