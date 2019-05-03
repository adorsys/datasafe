package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;

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
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, location.location().getPath());
        S3Object fullObject = s3.getObject(getObjectRequest);
        return fullObject.getObjectContent();
    }
}