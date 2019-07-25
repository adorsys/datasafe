package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class DFSCredentialsFactory {
    public static final String AMAZON_ENV = "SC-AMAZONS3";
    public static final String AMAZON_ENV_V2 = "SC-V2-AMAZONS3";
    public static final String FILESYSTEM_ENV = "SC-FILESYSTEM";
    private static final String DEFAULT_ROOT_BUCKET = "./target/default-filesystem-root";

    public static DFSCredentials getFromEnvironmnet() {
        DFSCredentials result = tryToCreateAmazonS3();
        if (null != result) {
            return result;
        }

        if (System.getProperty(FILESYSTEM_ENV) != null) {
            String value = System.getProperty(FILESYSTEM_ENV);
            String[] parts = value.split(",");
            if (parts.length != 1) {
                throw new SimpleAdapterException("expected <rootbucket> for " + FILESYSTEM_ENV);
            }
            log.info("create DFSCredentials for FILESYSTEM to root bucket " + parts[0]);

            return FilesystemDFSCredentials.builder()
                    .root(parts[0]).build();
        }
        log.info("create DFSCredentials for FILESYSTEM to root bucket " + DEFAULT_ROOT_BUCKET);
        return FilesystemDFSCredentials.builder()
                .root(DEFAULT_ROOT_BUCKET).build();
    }

    private static DFSCredentials tryToCreateAmazonS3() {
        String value = Stream.of(System.getProperty(AMAZON_ENV_V2), System.getProperty(AMAZON_ENV))
                .filter(Objects::nonNull).findFirst().orElse(null);

        if (null == value) {
            return null;
        }

        String[] parts = value.split(",");
        if (parts.length != 5 && parts.length != 7) {
            throw new SimpleAdapterException("expected at least :" +
                    "<url>,<accesskey>,<secretkey>,<region>,<rootbucket> OR " +
                    "<url>,<accesskey>,<secretkey>,<region>,<rootbucket>,<use https>,<threadpool size> for " +
                    AMAZON_ENV);
        }
        log.info("create DFSCredentials for S3 to url " + parts[0] + " with root bucket " + parts[4]);
        AmazonS3DFSCredentials.AmazonS3DFSCredentialsBuilder builder = AmazonS3DFSCredentials.builder()
                .url(parts[0])
                .accessKey(parts[1])
                .secretKey(parts[2])
                .region(parts[3])
                .rootBucket(parts[4]);

        if (parts.length == 7) {
            builder.noHttps(Boolean.parseBoolean(parts[5])).threadPoolSize(Integer.parseInt(parts[6]));
        }

        return builder.build();
    }
}
