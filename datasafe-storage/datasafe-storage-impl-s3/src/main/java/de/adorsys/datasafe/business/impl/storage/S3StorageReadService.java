package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;

@Slf4j
public class S3StorageReadService implements StorageReadService {

    private final AmazonS3 s3;
    private final String bucketName;

    @Inject
    public S3StorageReadService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @SneakyThrows
    @Override
    public InputStream read(ResourceLocation location) {
        log.debug("Read from {}", location.location());
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, location.location().getPath().replaceFirst("^/", ""));
        S3Object fullObject = s3.getObject(getObjectRequest);
        return fullObject.getObjectContent();
    }
}