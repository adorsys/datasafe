package de.adorsys.datasafe.storage.impl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

@Slf4j
public class S3StorageService implements StorageService {

    private final AmazonS3 s3;
    private final String bucketName;
    private final ExecutorService executorService;

    @Inject
    public S3StorageService(AmazonS3 s3, String bucketName, ExecutorService executorService) {
        this.s3 = s3;
        this.bucketName = bucketName;
        this.executorService = executorService;
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location) {
        log.debug("List at {}", location);
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(bucketName);
        String prefix = location.location().getPath().replaceFirst("^/", "");
        int len = prefix.length();
        listObjectsV2Request.setPrefix(prefix);
        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        return objectSummaries.stream()
                .map(os -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                createResource(location, os, len),
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
        log.debug("Write data by path: {}", Log.secure(location.location()));
        return new MultipartUploadS3StorageOutputStream(bucketName, location.getResource(), s3, executorService);
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

        return BasePrivateResource.forPrivate(relUrl).resolve(root);
    }
}
