package org.davidmoten.io.extras;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<S, T> {

    T apply(S s) throws IOException;

}
