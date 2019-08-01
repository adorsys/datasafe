package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Storage connection pool that creates S3/or other clients on the fly, based on provided credentials in URI.
 * URI format that is expected:
 * s3://access-key:secret-key@bucket/path/in/bucket
 */
@RequiredArgsConstructor
public class UriBasedAuthStorageService implements StorageService {

    private final Map<AccessId, StorageService> clientByItsAccessKey = new ConcurrentHashMap<>();
    private final Function<URI, String> bucketExtractor;
    private final Function<URI, String> endpointExtractor;

    /**
     * Expects bucket name to be first part of URI part, and endpoint to host + bucket name.
     */
    public UriBasedAuthStorageService(Function<AccessId, StorageService> storageServiceBuilder) {
        this.storageServiceBuilder = storageServiceBuilder;
        this.bucketExtractor = location -> location.getPath().replaceAll("^/", "").split("/")[0];
        this.endpointExtractor = location ->
            location.getScheme() + "://" + location.getHost() + ":" + location.getPort() + "/" +
                this.bucketExtractor.apply(location);
    }

    // Builder to create S3 or other kind of Storage service
    private final Function<AccessId, StorageService> storageServiceBuilder;

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        return storage(location).objectExists(location);
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        return storage(location).list(location);
    }

    @Override
    public InputStream read(AbsoluteLocation location) {
        return storage(location).read(location);
    }

    @Override
    public void remove(AbsoluteLocation location) {
        storage(location).remove(location);
    }

    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        return storage(locationWithCallback.getWrapped()).write(locationWithCallback);
    }

    private StorageService storage(AbsoluteLocation location) {
        String[] authority = location.location().asURI().getAuthority().split("@")[0].split(":");
        URI uri = location.getResource().location().withoutAuthority();
        AccessId accessId = new AccessId(
                authority[0],
                authority[1],
                bucketExtractor.apply(uri),
                endpointExtractor.apply(uri),
                uri,
                URI.create(
                    uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != - 1 ? ":" + uri.getPort() : "")
                )
        );

        return clientByItsAccessKey.computeIfAbsent(accessId, storageServiceBuilder);
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"accessKey", "secretKey", "bucketName", "endpoint"})
    public static class AccessId {

        private final String accessKey;
        private final String secretKey;
        private final String bucketName;
        private final String endpoint;

        private final URI withoutCreds;
        private final URI onlyHostPart;
    }
}
