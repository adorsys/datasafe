package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Builder
@Getter
public class FilesystemDFSCredentials extends DFSCredentials {
    private final Path root;

}
