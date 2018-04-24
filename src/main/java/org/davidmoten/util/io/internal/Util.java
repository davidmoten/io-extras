package org.davidmoten.util.io.internal;

import java.nio.ByteBuffer;

public final class Util {

    private Util() {
        // prevent instantiation
    }
    
    public static String toString(ByteBuffer bb) {
        StringBuilder s = new StringBuilder();
        int p = bb.position();
        while (bb.remaining() > 0) {
            if (s.length() > 0) {
                s.append(", ");
            }
            s.append(bb.get());
        }
        bb.position(p);
        return s.toString();
    }
    
    public static ByteBuffer copy(ByteBuffer bb) {
        byte[] a = new byte[bb.remaining()];
        bb.get(a);
        return ByteBuffer.wrap(a);
    }


}
