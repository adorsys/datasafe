package de.adorsys.datasafe.types.api.shared;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwsClientRetry {
    @SneakyThrows
    public static void createBucketWithRetry(AmazonS3 client, String bucket) {
        try {
            client.createBucket(bucket);
        } catch (AmazonS3Exception ex) {
            if (ex.getErrorCode().contains("MinioServerNotInitialized")) {
                long millis = 3000;
                log.info("we wait here for {} millis and retry due to eception :{} {}", millis, ex.getClass().toGenericString(), ex.getMessage());
                try {
                    Thread.sleep(millis);
                } catch (Exception e) {
                }
                log.info("sleep finished. now retry");
                client.createBucket(bucket);
            } else {
                log.info("exception by creating bucket does not look like expected");
                throw ex;
            }
        }
    }
}
