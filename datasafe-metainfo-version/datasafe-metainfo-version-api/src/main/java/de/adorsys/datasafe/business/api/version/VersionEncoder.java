package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.resource.*;

import java.net.URI;
import java.util.Optional;

/**
 * Encodes and decodes URI into/from URI with version.
 */
public interface VersionEncoder {

    /**
     * Generates URI tagged with version.
     * @param resource URI to tag with version
     * @return URI with version
     */
    VersionedUri newVersion(URI resource);

    /**
     * Parses versioned URI.
     * @param uri resource with encoded version
     * @return decoded resource and version
     * @apiNote While this method can accidentally misdetect version it is relatively safe.
     */
    Optional<VersionedUri> decodeVersion(URI uri);
}
