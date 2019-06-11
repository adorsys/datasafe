package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@AllArgsConstructor
public class DFSCredentials {
    private final Path root;
}
