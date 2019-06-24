package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Amazon S3, minio and CEPH compatible default S3 interface adapter.
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
     */
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        log.debug("List at {}", Obfuscate.secure(location));
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
        log.debug("Read from {}", Obfuscate.secure(key));
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        S3Object fullObject = s3.getObject(getObjectRequest);
        return fullObject.getObjectContent();
    }

    @Override
    public OutputStream write(AbsoluteLocation location) {
        log.debug("Write data by path: {}", Obfuscate.secure(location.location()));
        return new MultipartUploadS3StorageOutputStream(bucketName, location.getResource(), s3, executorService);
    }

    @Override
    public void remove(AbsoluteLocation location) {
        String path = location.location().getPath();
        String key = path.replaceFirst("^/", "").replaceFirst("/$", "");
        log.debug("Remove path {}", Obfuscate.secure(key));
        s3.deleteObject(bucketName, key);
    }

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        String path = location.location().getPath();
        String key = path.replaceFirst("^/", "").replaceFirst("/$", "");
        boolean pathExists = s3.doesObjectExist(bucketName, key);
        log.debug("Path {} exists {}", Obfuscate.secure(key), pathExists);
        return pathExists;
    }

    private PrivateResource createResource(AbsoluteLocation root, S3ObjectSummary os, int prefixLen) {
        String relUrl = os.getKey().substring(prefixLen).replaceFirst("^/", "");
        if ("".equals(relUrl)) {
            return BasePrivateResource.forPrivate(root.location());
        }

        return BasePrivateResource.forPrivate(relUrl).resolveFrom(root);
    }
}
