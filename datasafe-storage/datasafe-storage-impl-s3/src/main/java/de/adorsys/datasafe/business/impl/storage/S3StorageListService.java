package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

public class S3StorageListService implements StorageListService {

    private final AmazonS3 s3;
    private final String bucketName;

    @Inject
    public S3StorageListService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @SneakyThrows
    @Override
    public Stream<PrivateResource> list(ResourceLocation location) {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.setBucketName(bucketName);
        listObjectsV2Request.setPrefix(location.location().getPath());
        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(listObjectsV2Request);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        return objectSummaries.stream().map(os -> new DefaultPrivateResource(location.location().relativize(URI.create(os.getKey()))));
    }
}