package de.adorsys.datasafe.types.api.shared;

import com.amazonaws.services.s3.AmazonS3;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Duration;

import static org.awaitility.Awaitility.await;

@Slf4j
public class AwsClientRetry {
    @SneakyThrows
    public static void createBucketWithRetry(AmazonS3 client, String bucket) {
        FinalCounter counter = new FinalCounter(0);
        await().atMost(Duration.TEN_SECONDS).pollInterval(Duration.ONE_SECOND).untilAsserted(() -> {
            log.info("this is the {} try to create bucket", counter.inc().getCounter());
            client.createBucket(bucket);
        });
    }

    @Getter
    @AllArgsConstructor
    static class FinalCounter {
        int counter;
        public FinalCounter inc() {
            counter++;
            return this;
        }
    }
}
