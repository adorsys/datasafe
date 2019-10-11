package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

import java.util.function.Supplier;

public class ReadKeyPasswordHelper {
    public static ReadKeyPassword getForString(String a) {
        return new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return a.toCharArray();
            }
        });
    }
}
