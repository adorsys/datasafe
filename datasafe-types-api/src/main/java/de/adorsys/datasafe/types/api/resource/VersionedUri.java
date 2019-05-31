package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is the wrapper for URI resource that contains a version tag.
 */
@Getter
@RequiredArgsConstructor
public class VersionedUri {

    /**
     * Path to the resource, that contains version (typically physical path to the resource).
     */
    private final Uri pathWithVersion;

    /**
     * Path to the resource without version (typically logical path used to identify latest resource version)
     */
    private final Uri pathWithoutVersion;

    /**
     * Version associated with the resource
     */
    private final String version;
}
