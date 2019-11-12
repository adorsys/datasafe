package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

public class ReadKeyPasswordTestFactory {

    public static ReadKeyPassword getForString(String a) {
        return new ReadKeyPassword(a::toCharArray);
    }
}
