package org.davidmoten.io.extras;

import java.io.IOException;

public final class BoundExceededException extends IOException {

    private static final long serialVersionUID = 4420898379814459657L;
    
    public BoundExceededException(String message) {
        super(message);
    }

}
