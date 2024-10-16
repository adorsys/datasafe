package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class StaticBucketRouterTest {

    private StaticBucketRouter router;

    @BeforeEach
    void setup() {
        String region = "region";
        String bucketName = "bucket";
        router = new StaticBucketRouter(region, bucketName);
    }

    @Test
    void resourceKeyTest() {
        var root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("http://s3-us-west-2.amazonaws.com/bucket/users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes")));
        log.info(String.valueOf(root));
        String resourcePath = router.resourceKey(root);
        assertThat(resourcePath).hasToString("users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes");
    }

    @Test
    void noBucketInPath() {
        var root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("http://bucket.s3-us-west-2.amazonaws.com/users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes")));
        String resourcePath = router.resourceKey(root);
        assertThat(resourcePath).hasToString("users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes");
    }

    @Test
    void regionAndBucketInPath() {
        var root = new AbsoluteLocation<>(BasePrivateResource.forPrivate(new Uri("s3://host/region/bucket/users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes")));
        String resourcePath = router.resourceKey(root);
        assertThat(resourcePath).hasToString("users/myuserid/private/files/bucket/users/otheruser/private/files/somefile.aes");
    }
}
