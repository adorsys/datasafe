package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;

@Slf4j
public class DFSCredentialsFactory {
    public final static String AMAZON_ENV = "SC-AMAZONS3";
    public final static String FILESYSTEM_ENV = "SC-FILESYSTEM";
    private final static String DEFAULT_ROOT_BUCKET = "./target/default-filesystem-root";

    public static DFSCredentials getFromEnvironmnet() {
        if (System.getProperty(AMAZON_ENV) != null) {
            String value = System.getProperty(AMAZON_ENV);
            String[] parts = value.split(",");
            if (parts.length != 5) {
                throw new SimpleAdapterException("expected <url>,<accesskey>,<secretkey>,<region>,<rootbucket> for " + AMAZON_ENV);
            }
            log.info("create DFSCredentials for S3 to url " + parts[0] + " with root bucket " + parts[4]);
            return AmazonS3DFSCredentials.builder()
                    .url(parts[0])
                    .accessKey(parts[1])
                    .secretKey(parts[2])
                    .region(parts[3])
                    .rootBucket(parts[4]).build();

        }
        if (System.getProperty(FILESYSTEM_ENV) != null) {
            String value = System.getProperty(FILESYSTEM_ENV);
            String[] parts = value.split(",");
            if (parts.length != 1) {
                throw new SimpleAdapterException("expected <rootbucket> for " + FILESYSTEM_ENV);
            }
            log.info("create DFSCredentials for FILESYSTEM to root bucket " + parts[0]);

            return FilesystemDFSCredentials.builder()
                    .root(FileSystems.getDefault().getPath(parts[0])).build();
        }
        log.info("create DFSCredentials for FILESYSTEM to root bucket " + DEFAULT_ROOT_BUCKET);
        return FilesystemDFSCredentials.builder()
                .root(FileSystems.getDefault().getPath(DEFAULT_ROOT_BUCKET)).build();
    }
}
