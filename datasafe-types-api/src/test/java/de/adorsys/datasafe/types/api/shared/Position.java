package de.adorsys.datasafe.types.api.shared;

import java.util.stream.Stream;

public final class Position {

    private Position() {
    }

    public static <T> T first(Stream<T> data) {
        return data.findFirst().orElseThrow(() -> new IllegalArgumentException("No first element"));
    }
}
