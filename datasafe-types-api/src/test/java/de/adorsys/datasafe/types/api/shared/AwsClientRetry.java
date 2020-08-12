package de.adorsys.datasafe.types.api.shared;

import com.amazonaws.services.s3.AmazonS3;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Duration;

import static org.awaitility.Awaitility.await;

@Slf4j
public class AwsClientRetry {
    @SneakyThrows
    public static void createBucketWithRetry(AmazonS3 client, String bucket) {
        RetryLogger logger = new RetryLogger();
        await().atMost(Duration.TEN_SECONDS).pollInterval(Duration.ONE_SECOND).untilAsserted(() -> {
            logger.log();
            client.createBucket(bucket);
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
