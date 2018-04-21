package org.davidmoten.util.io;

import java.io.IOException;

@FunctionalInterface
public interface FunctionCanThrow<S, T> {

    T apply(S s) throws IOException;

}
