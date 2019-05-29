package de.adorsys.datasafe.metainfo.version.impl.version.latest;

import de.adorsys.datasafe.metainfo.version.api.version.VersionEncoderDecoder;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.VersionedUri;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * Encoder/decoder that creates URI versions using UUID generator and path separator, so that versioned resource
 * http://example.com/some/path--75943a83-ae8a-4eaf-bffb-1a20f235416c
 * means version 75943a83-ae8a-4eaf-bffb-1a20f235416c of http://example.com/some/path
 */
public class DefaultVersionEncoderDecoder implements VersionEncoderDecoder {

    private static final String SEPARATOR = "--";

    @Inject
    public DefaultVersionEncoderDecoder() {
    }

    public VersionedUri newVersion(URI resource) {
        String version = UUID.randomUUID().toString();

        return new VersionedUri(
                Uri.build(resource.toASCIIString() + SEPARATOR + version),
                resource,
                version
        );
    }

    public Optional<VersionedUri> decodeVersion(URI uri) {
        String[] parts = uri.getPath().split("/");
        String name = parts[parts.length - 1];
        String[] withUuid = name.split(SEPARATOR, 2);

        if (withUuid.length != 2) {
            return Optional.empty();
        }

        try {
            UUID uuid = UUID.fromString(withUuid[1]);
            return Optional.of(
                    new VersionedUri(
                            uri,
                            uri.resolve("./" + withUuid[0]),
                            uuid.toString())
            );
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
