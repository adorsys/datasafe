package de.adorsys.datasafe.metainfo.version.impl.version;

import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.VersionedUri;

import java.util.Optional;

/**
 * Encodes and decodes URI into/from URI with version.
 */
public interface VersionEncoderDecoder {

    /**
     * Generates URI tagged with version.
     * @param resource URI to tag with version
     * @return URI with version
     */
    VersionedUri newVersion(Uri resource);

    /**
     * Parses versioned URI.
     * @param uri resource with encoded version
     * @return decoded resource and version
     * @apiNote It won't work on non-versioned resources.
     */
    Optional<VersionedUri> decodeVersion(Uri uri);
}
