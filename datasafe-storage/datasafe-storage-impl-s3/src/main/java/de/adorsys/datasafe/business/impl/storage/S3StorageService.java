package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class S3StorageService implements StorageService {

    private final AmazonS3 s3;
    private final String bucketName;

    @Inject
    public S3StorageService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public Stream<AbsoluteResourceLocation<PrivateResource>> list(AbsoluteResourceLocation location) {
        log.debug("List at {}", location.location());
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(bucketName);
        String prefix = location.location().getPath().replaceFirst("^/", "");
        int len = prefix.length();
        listObjectsV2Request.setPrefix(prefix);
        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        return objectSummaries.stream()
                .map(os -> {
                    URI uri = URI.create(os.getKey().substring(len));
                    return new AbsoluteResourceLocation<>(new DefaultPrivateResource(uri).resolve(location));
                });
    }

    @Override
    public InputStream read(AbsoluteResourceLocation location) {
        log.debug("Read from {}", location.location());
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, location.location().getPath().replaceFirst("^/", ""));
        S3Object fullObject = s3.getObject(getObjectRequest);
        return fullObject.getObjectContent();
    }

    @Override
    public OutputStream write(AbsoluteResourceLocation location) {
        return new PutBlobOnClose(s3, bucketName, location);
    }

    @Override
    public void remove(AbsoluteResourceLocation location) {
        String path = location.location().getPath();
        log.debug("Remove path {}", path);
        s3.deleteObject(bucketName, path.replaceFirst("^/", "").replaceFirst("/$", ""));
    }

    @Slf4j
    @RequiredArgsConstructor
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final AmazonS3 s3;
        private final String bucketName;
        private final ResourceLocation resource;

        @Override
        public void close() throws IOException {

            log.debug("Write to {}", resource.location());
            ObjectMetadata metadata = new ObjectMetadata();
            byte[] data = super.toByteArray();
            metadata.setContentLength(data.length);

            InputStream is = new ByteArrayInputStream(data);

            s3.putObject(
                    bucketName,
                    resource.location().getPath().replaceFirst("^/", ""),
                    is,
                    metadata
            );

            super.close();
        }
    }
}
