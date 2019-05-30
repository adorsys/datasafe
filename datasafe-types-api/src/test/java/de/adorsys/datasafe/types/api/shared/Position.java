package de.adorsys.datasafe.types.api.shared;

import java.util.stream.Stream;

/**
 * Utility class for getting the element at the position from a stream.
 */
public final class Position {

    private Position() {
    }

    /**
     * Returns first element from stream.
     * @param data Stream from which to get element
     * @param <T> Generic
     * @return First element of the stream, or throws {@link IllegalArgumentException} if stream is empty.
     */
    public static <T> T first(Stream<T> data) {
        return data.findFirst().orElseThrow(() -> new IllegalArgumentException("No first element"));
    }
}
