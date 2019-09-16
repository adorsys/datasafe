package de.adorsys.datasafe.metainfo.version.impl.version;

import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.VersionedUri;

import java.util.Optional;

/**
 * Encodes and decodes URI into/from URI with appVersion.
 */
public interface VersionEncoderDecoder {

    /**
     * Generates URI tagged with appVersion.
     * @param resource URI to tag with appVersion
     * @return URI with appVersion
     */
    VersionedUri newVersion(Uri resource);

    /**
     * Parses versioned URI.
     * @param uri resource with encoded appVersion
     * @return decoded resource and appVersion
     * @apiNote It won't work on non-versioned resources.
     */
    Optional<VersionedUri> decodeVersion(Uri uri);
}
