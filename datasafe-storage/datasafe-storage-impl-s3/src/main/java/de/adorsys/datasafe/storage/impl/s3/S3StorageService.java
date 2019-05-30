package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Amazon S3, minio and CEPH compatible default S3 interface adapter.
 */
@Slf4j
public class S3StorageService implements StorageService {

    private final AmazonS3 s3;
    private final String bucketName;

    /**
     * @param s3 Connection to S3
     * @param bucketName Bucket to use
     */
    @Inject
    public S3StorageService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    /**
     * Lists all resources within bucket and returns absolute resource location for each entry without credentials.
     */
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        log.debug("List at {}", Log.secure(location));
        String prefix = location.location().getPath().replaceFirst("^/", "");

        S3Objects s3ObjectSummaries = S3Objects.withPrefix(s3, bucketName, prefix);
        Stream<S3ObjectSummary> objectStream = StreamSupport.stream(s3ObjectSummaries.spliterator(), false);
        return objectStream
                .map(os -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                createResource(location, os, prefix.length()),
                                os.getLastModified().toInstant()
                        ))
                );
    }

    @Override
    public InputStream read(AbsoluteLocation location) {
        String key = location.location().getPath().replaceFirst("^/", "");
        log.debug("Read from {}", Log.secure(key));
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        S3Object fullObject = s3.getObject(getObjectRequest);
        return fullObject.getObjectContent();
    }

    @Override
    public OutputStream write(AbsoluteLocation location) {
        return new PutBlobOnClose(s3, bucketName, location);
    }

    @Override
    public void remove(AbsoluteLocation location) {
        String path = location.location().getPath();
        String key = path.replaceFirst("^/", "").replaceFirst("/$", "");
        log.debug("Remove path {}", Log.secure(key));
        s3.deleteObject(bucketName, key);
    }

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        String path = location.location().getPath();
        String key = path.replaceFirst("^/", "").replaceFirst("/$", "");
        boolean pathExists = s3.doesObjectExist(bucketName, key);
        log.debug("Path {} exists {}", Log.secure(key), pathExists);
        return pathExists;
    }

    private PrivateResource createResource(AbsoluteLocation root, S3ObjectSummary os, int prefixLen) {
        String relUrl = os.getKey().substring(prefixLen).replaceFirst("^/", "");
        if ("".equals(relUrl)) {
            return BasePrivateResource.forPrivate(root.location());
        }

        return BasePrivateResource.forPrivate(relUrl).resolveFrom(root);
    }

    /**
     * Helper class that allows us to work with streams by collecting them into byte array and writing those bytes
     * when stream is closed.
     */
    @Slf4j
    @RequiredArgsConstructor
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final AmazonS3 s3;
        private final String bucketName;
        private final ResourceLocation resource;

        @Override
        public void close() throws IOException {

            ObjectMetadata metadata = new ObjectMetadata();
            byte[] data = super.toByteArray();
            metadata.setContentLength(data.length);

            InputStream is = new ByteArrayInputStream(data);

            String key = resource.location().getPath().replaceFirst("^/", "");
            log.debug("Write to {}", Log.secure(key));
            s3.putObject(bucketName, key, is, metadata);

            super.close();
        }
    }
}
