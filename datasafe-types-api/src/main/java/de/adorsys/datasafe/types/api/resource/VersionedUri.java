package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@Getter
@RequiredArgsConstructor
public class VersionedUri {

    private final URI pathWithVersion;
    private final URI pathWithoutVersion;
    private final String version;
}
