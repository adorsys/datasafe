package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReadKeyPasswordHelper {
    public static ReadKeyPassword getForString(String a) {
        return new ReadKeyPassword(a::toCharArray);
    }
}
