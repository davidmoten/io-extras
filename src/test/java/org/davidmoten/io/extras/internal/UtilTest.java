package org.davidmoten.io.extras.internal;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.davidmoten.io.extras.internal.Util;
import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class UtilTest {
    
    @Test
    public void testToString() {
        byte[] b = new byte[] {100, -23, 34, 15};
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.position(1);
        bb.limit(3);
        assertEquals("-23, 34", Util.toString(bb));
    }
    
    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(Util.class);
    }
}
