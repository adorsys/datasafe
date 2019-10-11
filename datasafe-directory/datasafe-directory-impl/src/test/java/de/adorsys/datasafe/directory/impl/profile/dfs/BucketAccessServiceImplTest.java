package de.adorsys.datasafe.directory.impl.profile.dfs;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;

class BucketAccessServiceImplTest extends BaseMockitoTest {

    private static final String ABSOLUTE_BUCKET = "s3://bucket";

    private UserIDAuth auth = new UserIDAuth(new UserID(""), ReadKeyPasswordTestFactory.getForString(""));

    @InjectMocks
    private BucketAccessServiceImpl bucketAccessService;

    @Test
    void privateAccessFor() {
        assertThat(bucketAccessService.privateAccessFor(
                auth,
                BasePrivateResource.forPrivate(ABSOLUTE_BUCKET)).location().asURI()
        ).asString().isEqualTo(ABSOLUTE_BUCKET);
    }

    @Test
    void publicAccessFor() {
        assertThat(bucketAccessService.publicAccessFor(
                auth.getUserID(),
                BasePublicResource.forAbsolutePublic(ABSOLUTE_BUCKET).getResource()).location().asURI()
        ).asString().isEqualTo(ABSOLUTE_BUCKET);
    }
}
