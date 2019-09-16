package de.adorsys.datasafe.metainfo.version.impl.version.latest;

import de.adorsys.datasafe.metainfo.version.impl.version.VersionEncoderDecoder;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.VersionedUri;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Encoder/decoder that creates URI versions using UUID generator and path separator, so that versioned resource
 * http://example.com/some/path/75943a83-ae8a-4eaf-bffb-1a20f235416c
 * means version 75943a83-ae8a-4eaf-bffb-1a20f235416c of http://example.com/some/path
 */
@RuntimeDelegate
public class DefaultVersionEncoderDecoder implements VersionEncoderDecoder {

    private static final String SEPARATOR = "/";

    @Inject
    public DefaultVersionEncoderDecoder() {
    }

    @Override
    public VersionedUri newVersion(Uri resource) {
        String version = UUID.randomUUID().toString();

        return new VersionedUri(
                new Uri(resource.toASCIIString() + SEPARATOR + version),
                resource,
                version
        );
    }

    @Override
    public Optional<VersionedUri> decodeVersion(Uri uri) {
        String[] parts = uri.getRawPath().split(SEPARATOR);

        if (parts.length < 2) {
            return Optional.empty();
        }

        try {
            UUID uuid = UUID.fromString(parts[parts.length - 1]);
            return Optional.of(
                    new VersionedUri(
                            uri,
                            uri.resolve(".").asFile(),
                            uuid.toString())
            );
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
