package de.adorsys.datasafe.rest.impl.controller;

import com.google.common.io.Resources;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class Fixture {

    @SneakyThrows
    public String read(String path) {
        return Resources
            .asCharSource(Resources.getResource(path), StandardCharsets.UTF_8)
            .read();
    }
}
