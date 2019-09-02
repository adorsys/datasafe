package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.Getter;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

//@AllArgsConstructor

//TODO uncomment above
@Getter
public class Counter {
    private final byte[] value = new byte[16];

    public Counter() {
        Arrays.fill(value, (byte)'a');
        //ThreadLocalRandom.current().nextBytes(value);
    }
}
