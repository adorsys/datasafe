package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import java.util.function.Supplier;

public class ReadKeyPasswordTestFactory {
    public static ReadKeyPassword getForString(String a) {
        return new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return a.toCharArray();
            }
        });
    }
}
