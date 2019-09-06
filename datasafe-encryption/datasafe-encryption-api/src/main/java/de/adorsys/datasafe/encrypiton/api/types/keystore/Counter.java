package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Counter {

    private byte[] value; //size should be 16 bytes

    //TODO: remove empty constructor? think about it
    public Counter() {
        value = new byte[16];
    }

}