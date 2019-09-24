package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode(of = "root", callSuper = false)
public class FilesystemDFSCredentials extends DFSCredentials {
    private final String root;
}
