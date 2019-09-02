package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Counter {
    private final byte[] value;
}
