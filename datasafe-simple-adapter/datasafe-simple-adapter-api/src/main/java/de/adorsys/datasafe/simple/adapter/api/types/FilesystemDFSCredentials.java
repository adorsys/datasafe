package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FilesystemDFSCredentials extends DFSCredentials {
    @Builder
    public FilesystemDFSCredentials(Path root) {
        super(root);
    }
}
