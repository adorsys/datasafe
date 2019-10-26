package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Storage connection pool that creates S3/or other clients on the fly, based on provided credentials in URI.
 * URI format that is expected:
 * s3://access-key:secret-key@region/bucket/path/in/bucket
 */
@RequiredArgsConstructor
public class UriBasedAuthStorageService extends BaseDelegatingStorage {

    private final Map<AccessId, StorageService> clientByItsAccessKey = new ConcurrentHashMap<>();
    private final Function<URI, String> bucketExtractor;
    private final Function<URI, String> regionExtractor;
    private final Function<URI, String> endpointExtractor;

    // Builder to create S3 or other kind of Storage service
    private final Function<AccessId, StorageService> storageServiceBuilder;

    /**
     * Expects bucket name to be first part of URI part, and endpoint to host + bucket name.
     */
    public UriBasedAuthStorageService(Function<AccessId, StorageService> storageServiceBuilder) {
        this.storageServiceBuilder = storageServiceBuilder;
        Function<URI, String[]> segmentator = location -> location.getPath().replaceAll("^/", "").split("/");
        this.regionExtractor = location -> segmentator.apply(location)[0];
        this.bucketExtractor = location -> segmentator.apply(location)[1];
        this.endpointExtractor = location ->
            location.getScheme() + "://" + location.getHost() + portValue(location) + "/";
    }

    public UriBasedAuthStorageService(Function<AccessId, StorageService> storageServiceBuilder,
                                      Function<URI, String[]> segmentator) {
        this.storageServiceBuilder = storageServiceBuilder;
        this.regionExtractor = location -> segmentator.apply(location)[0];
        this.bucketExtractor = location -> segmentator.apply(location)[1];
        this.endpointExtractor = location ->
                location.getScheme() + "://" + location.getHost() + portValue(location) + "/";
    }

    @Override
    protected StorageService service(AbsoluteLocation location) {
        String[] authority = location.location().asURI().getAuthority().split("@")[0].split(":");
        URI uri = location.getResource().location().withoutAuthority();
        AccessId accessId = new AccessId(
                authority[0],
                authority[1],
                regionExtractor.apply(uri),
                bucketExtractor.apply(uri),
                endpointExtractor.apply(uri),
                uri,
                URI.create(
                    uri.getScheme() + "://" + uri.getHost() + portValue(uri)
                )
        );

        return clientByItsAccessKey.computeIfAbsent(accessId, storageServiceBuilder);
    }

    private String portValue(URI uri) {
        return uri.getPort() != - 1 ? ":" + uri.getPort() : "";
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"accessKey", "secretKey", "bucketName", "endpoint"})
    public static class AccessId {

        private final String accessKey;
        private final String secretKey;
        private final String region;
        private final String bucketName;
        private final String endpoint;

        private final URI withoutCreds;
        private final URI onlyHostPart;
    }
}
