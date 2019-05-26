package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class BucketAccessServiceImplTest extends BaseMockitoTest {

    private static final String ABSOLUTE_BUCKET = "s3://bucket";

    private UserIDAuth auth = new UserIDAuth(new UserID(""), new ReadKeyPassword(""));

    @InjectMocks
    private BucketAccessServiceImpl bucketAccessService;

    @Test
    void privateAccessFor() {
        assertThat(bucketAccessService.privateAccessFor(
                auth,
                BasePrivateResource.forPrivate(URI.create(ABSOLUTE_BUCKET))).location()
        ).asString().isEqualTo(ABSOLUTE_BUCKET);
    }

    @Test
    void publicAccessFor() {
        assertThat(bucketAccessService.publicAccessFor(
                auth.getUserID(),
                BasePublicResource.forAbsolutePublic(URI.create(ABSOLUTE_BUCKET))).location()
        ).asString().isEqualTo(ABSOLUTE_BUCKET);
    }
}
