package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class FilesystemDFSCredentials extends DFSCredentials {
    private final String root;
}
