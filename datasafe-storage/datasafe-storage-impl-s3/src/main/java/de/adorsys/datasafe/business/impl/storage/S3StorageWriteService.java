package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.*;

public class S3StorageWriteService implements StorageWriteService {

    private final AmazonS3 s3;
    private final String bucketName;

    @Inject
    public S3StorageWriteService(AmazonS3 s3, String bucketName) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @SneakyThrows
    @Override
    public OutputStream write(ResourceLocation location) {
        return new PutBlobOnClose(s3, bucketName, location);
    }

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

            s3.putObject(
                    bucketName,
                    resource.location().getPath(),
                    is,
                    metadata
            );

            super.close();
        }
    }
}