package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.StorageVersion;
import de.adorsys.datasafe.types.api.resource.VersionedResourceLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Amazon S3, minio and CEPH compatible default S3 interface adapter.
 * Note: It is using rawPath of URI that is url-encoded due to:
 * https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html
 */
@Slf4j
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final BucketRouter router;
    private final ExecutorService executorService;

    /**
     * @param s3 Connection to S3
     * @param bucketName Bucket to use
     * @param executorService Multipart sending threadpool (file chunks are sent in parallel)
     */
    public S3StorageService(S3Client s3, String bucketName, ExecutorService executorService) {
        this.s3 = s3;
        this.router = new StaticBucketRouter(bucketName);
        this.executorService = executorService;
    }

    /**
     * Lists all resources within bucket and returns absolute resource location for each entry without credentials.
     * Does not include object versions, only latest are shown.
     */
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        log.debug("List at {}", location.location());
        String prefix = router.resourceKey(location);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(router.bucketName(location))
                .prefix(prefix)
                .build();

        ListObjectsV2Response response = s3.listObjectsV2(request);
        Stream<S3Object> objectStream = response.contents().stream();
        return objectStream
                .map(os -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                createPath(location, os, prefix.length()),
                                os.lastModified()
                        ))
                );
    }

    /**
     * Reads resource by its location and uses its version if available in {@code location}
     */
    @Override
    public InputStream read(AbsoluteLocation location) {
        log.debug("Read from {}", location);

        String bucketName = router.bucketName(location);
        return executeAndReturn(
                location,
                key -> s3.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()).asInputStream(),
                (key, version) -> s3.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .versionId(version.getVersionId())
                        .build()).asInputStream()
        );
    }

    /**
     * Writes data stream into resource and sends resource version into callback if S3 returns version id.
     */
    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        log.debug("Write data by path: {}", locationWithCallback.getWrapped().location());

        String bucketName = router.bucketName(locationWithCallback.getWrapped());
        String key = router.resourceKey(locationWithCallback.getWrapped()); // <--- Define key here
        return new MultipartUploadS3StorageOutputStream(
                bucketName, // String
                key, // String
                s3, // S3Client
                executorService, // ExecutorService
                locationWithCallback.getWrapped().location().getPath(), // String
                locationWithCallback.getCallbacks() // List of ResourceWriteCallback
        );
    }
    /**
     * Removes resource by its location (latest one if versioning enabled) or removes just one version of the
     * resource if version if available in {@code location}
     */
    @Override
    public void remove(AbsoluteLocation location) {
        log.debug("Remove from {}", location);

        String bucketName = router.bucketName(location);
        execute(
                location,
                key -> s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build()),
                (key, version) -> s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).versionId(version.getVersionId()).build())
        );
    }

    /**
     * Checks if resource exists by its location (latest one if versioning enabled)
     * or checks if resource with given version exists if version is available in {@code location}
     */
    @Override
    public boolean objectExists(AbsoluteLocation location) {
        String bucketName = router.bucketName(location);

        boolean pathExists = executeAndReturn(
                location,
                key -> s3.listObjects(ListObjectsRequest.builder().bucket(bucketName).prefix(key).maxKeys(1).build()).contents().size() > 0,
                (key, version) -> s3.listObjectVersions(ListObjectVersionsRequest.builder().bucket(bucketName).prefix(key).versionIdMarker(version.getVersionId()).maxKeys(1).build())
                        .versions().stream().anyMatch(v -> v.versionId().equals(version.getVersionId()))
        );

        log.debug("Path {} exists {}", location, pathExists);
        return pathExists;
    }


    @Override
    public Optional<Integer> flushChunkSize(AbsoluteLocation location) {
        return Optional.of(MultipartUploadS3StorageOutputStream.BUFFER_SIZE);
    }

    private void doRemove(String bucket, String key) {
        if (key.endsWith("/")) {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(key)
                    .build();
            ListObjectsV2Response response = s3.listObjectsV2(request);
            response.contents().forEach(obj -> s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(obj.key())
                    .build()));
            return;
        }

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private PrivateResource createPath(AbsoluteLocation root, S3Object os, int prefixLen) {
        String relUrl = os.key().substring(prefixLen).replaceFirst("^/", "");
        if ("".equals(relUrl)) {
            return BasePrivateResource.forPrivate(root.location());
        }

        return BasePrivateResource.forPrivate(URI.create(relUrl)).resolveFrom(root);
    }

    private void execute(AbsoluteLocation location,
                          Consumer<String> ifNoVersion,
                          BiConsumer<String, StorageVersion> ifVersion) {

        executeAndReturn(
                location,
                path -> {
                    ifNoVersion.accept(path);
                    return null;
                },
                (path, version) -> {
                    ifVersion.accept(path, version);
                    return null;
                });
    }

    private <T> T executeAndReturn(AbsoluteLocation location,
                         Function<String, T> ifNoVersion,
                         BiFunction<String, StorageVersion, T> ifVersion) {

        String key = router.resourceKey(location);
        Optional<StorageVersion> version = extractVersion(location);

        if (!version.isPresent()) {
            return ifNoVersion.apply(key);
        }

        return ifVersion.apply(key, version.get());
    }

    private Optional<StorageVersion> extractVersion(AbsoluteLocation location) {
        if (!(location.getResource() instanceof VersionedResourceLocation)) {
            return Optional.empty();
        }

        VersionedResourceLocation withVersion = (VersionedResourceLocation) location.getResource();

        if (!(withVersion.getVersion() instanceof StorageVersion)) {
            return Optional.empty();
        }

        return Optional.of((StorageVersion) withVersion.getVersion());
    }
}
