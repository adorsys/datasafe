package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.datasafe.business.api.types.utils.Log;
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
        log.debug("List at {}", Log.secure(location.location()));
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(bucketName);
        String prefix = location.location().getPath().replaceFirst("^/", "");
        int len = prefix.length();
        listObjectsV2Request.setPrefix(prefix);
        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        return objectSummaries.stream()
                .map(os -> URI.create(os.getKey().substring(len)))
                .map(uri -> new DefaultPrivateResource(uri).resolve(location))
                .map(AbsoluteResourceLocation::new);
    }

    @Override
    public InputStream read(AbsoluteResourceLocation location) {
        String key = location.location().getPath().replaceFirst("^/", "");
        log.debug("Read from {}", Log.secure(key));
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
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
        String key = path.replaceFirst("^/", "").replaceFirst("/$", "");
        log.debug("Remove path {}", Log.secure(key));
        s3.deleteObject(bucketName, key);
    }

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
