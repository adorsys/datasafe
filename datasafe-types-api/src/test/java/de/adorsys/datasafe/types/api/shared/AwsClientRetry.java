package de.adorsys.datasafe.types.api.shared;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Duration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;

import static org.awaitility.Awaitility.await;

@Slf4j
public class AwsClientRetry {
    @SneakyThrows
    public static void createBucketWithRetry(S3Client client, String bucket) {
        RetryLogger logger = new RetryLogger();
        await().atMost(Duration.TEN_SECONDS).pollInterval(Duration.ONE_SECOND).untilAsserted(() -> {
            logger.log();
            CreateBucketResponse response = client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucket)
                    .build());
        });
    }

    @NoArgsConstructor
    static class RetryLogger {
        int counter = 0;
        public void log() {
            if (counter > 0) {
                log.info("this is the {} retry to create bucket", counter);
            }
            counter++;
        }
    }
}
