package de.adorsys.datasafe.business.impl.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.*;

@Slf4j
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