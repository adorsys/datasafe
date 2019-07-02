package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.iterable.S3Versions;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
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
public class S3StorageService implements StorageService {

    private final AmazonS3 s3;
    private final String bucketName;
    private final ExecutorService executorService;

    /**
     * @param s3 Connection to S3
     * @param bucketName Bucket to use
     * @param executorService Multipart sending threadpool (file chunks are sent in parrallel)
     */
    @Inject
    public S3StorageService(AmazonS3 s3, String bucketName, ExecutorService executorService) {
        this.s3 = s3;
        this.bucketName = bucketName;
        this.executorService = executorService;
    }

    /**
     * Lists all resources within bucket and returns absolute resource location for each entry without credentials.
     * Does not include object versions, only latest are shown.
     */
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        log.debug("List at {}", location.location());
        String prefix = location.location().getRawPath().replaceFirst("^/", "");

        S3Objects s3ObjectSummaries = S3Objects.withPrefix(s3, bucketName, prefix);
        Stream<S3ObjectSummary> objectStream = StreamSupport.stream(s3ObjectSummaries.spliterator(), false);
        return objectStream
                .map(os -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                createPath(location, os, prefix.length()),
                                os.getLastModified().toInstant()
                        ))
                );
    }

    /**
     * Reads resource by its location and uses its version if available in {@code location}
     */
    @Override
    public InputStream read(AbsoluteLocation location) {
        log.debug("Read from {}", location);

        return executeAndReturn(
                location,
                key -> s3.getObject(bucketName, key).getObjectContent(),
                (key, version) ->
                        s3.getObject(
                                new GetObjectRequest(bucketName, key, version.getVersionId())
                        ).getObjectContent()
        );
    }

    /**
     * Writes data stream into resource and sends resource version into callback if S3 returns version id.
     */
    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        log.debug("Write data by path: {}", locationWithCallback.getWrapped().location());

        return new MultipartUploadS3StorageOutputStream(
                bucketName,
                locationWithCallback.getWrapped().getResource(),
                s3,
                executorService,
                locationWithCallback.getCallbacks()
        );
    }

    /**
     * Removes resource by its location (latest one if versioning enabled) or removes just one version of the
     * resource if version if available in {@code location}
     */
    @Override
    public void remove(AbsoluteLocation location) {
        log.debug("Remove from {}", location);

        execute(
                location,
                key -> s3.deleteObject(bucketName, key),
                (key, version) -> s3.deleteVersion(bucketName, key, version.getVersionId())
        );
    }

    /**
     * Checks if resource exists by its location (latest one if versioning enabled)
     * or checks if resource with given version exists if version is available in {@code location}
     */
    @Override
    public boolean objectExists(AbsoluteLocation location) {
        boolean pathExists = executeAndReturn(
                location,
                path -> s3.doesObjectExist(bucketName, path),
                (path, version) ->
                        StreamSupport.stream(
                                S3Versions.withPrefix(s3, bucketName, path).spliterator(), false)
                                .anyMatch(it -> it.getVersionId().equals(version.getVersionId()))
        );

        log.debug("Path {} exists {}", location, pathExists);
        return pathExists;
    }

    private PrivateResource createPath(AbsoluteLocation root, S3ObjectSummary os, int prefixLen) {
        String relUrl = os.getKey().substring(prefixLen).replaceFirst("^/", "");
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

        String key = location.getResource().location()
                .getPath()
                .replaceFirst("^/", "")
                .replaceFirst("/$", "");
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
