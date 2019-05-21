package de.adorsys.datasafe.business.impl.version.latest;

import de.adorsys.datasafe.business.api.types.resource.VersionedUri;
import de.adorsys.datasafe.business.api.version.VersionEncoder;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class DefaultVersionEncoder implements VersionEncoder {

    private static final String SEPARATOR = "--";

    @Inject
    public DefaultVersionEncoder() {
    }

    public VersionedUri newVersion(URI resource) {
        String version = UUID.randomUUID().toString();

        return new VersionedUri(
                URI.create(resource.toASCIIString() + SEPARATOR + version),
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
