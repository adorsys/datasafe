package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import com.google.common.io.Resources;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that validates it is still possible to use old user profiles - when there was single private space.
 */
class ProfileRetrievalServiceImplForOldProfilesTest extends BaseMockitoTest {

    private static final AbsoluteLocation PRIV = BasePrivateResource.forAbsolutePrivate("file:///pathPriv");
    private static final AbsoluteLocation PUB = BasePrivateResource.forAbsolutePrivate("file:///pathPub");

    @Mock
    private DFSConfig dfsConfig;

    @Mock
    private StorageReadService readService;

    @Mock
    private StorageCheckService checkService;

    @Mock
    private BucketAccessService access;

    @Spy
    private GsonSerde serde = new GsonSerde(mock(PublicKeySerde.class));

    @Mock
    private UserProfileCache userProfileCache;

    @InjectMocks
    private ProfileRetrievalServiceImpl tested;

    @Mock
    private UserIDAuth user;

    @Mock
    private UserID userId;

    @BeforeEach
    @SneakyThrows
    void init() {
        when(user.getUserID()).thenReturn(userId);
        when(dfsConfig.publicProfile(userId)).thenReturn(PUB);
        when(dfsConfig.privateProfile(userId)).thenReturn(PRIV);
        when(access.withSystemAccess(any())).thenAnswer(inv -> inv.getArgument(0));
        when(readService.read(PRIV))
            .thenReturn(Resources.asByteSource(Resources.getResource("profile/user.priv")).openStream());
        when(readService.read(PUB))
            .thenReturn(Resources.asByteSource(Resources.getResource("profile/user.pub")).openStream());
    }

    @Test
    void publicProfile() {
        UserPublicProfile profile = tested.publicProfile(userId);

        assertThat(profile.getInbox()).extracting(this::str)
            .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/public/inbox/");
        assertThat(profile.getPublicKeys()).extracting(this::str)
            .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/public/pubkeys");
    }

    @Test
    void privateProfile() {
        UserPrivateProfile profile = tested.privateProfile(user);

        assertThat(profile.getKeystore()).extracting(this::str)
            .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/private/keystore");
        assertThat(profile.getPrivateStorage())
            .hasEntrySatisfying(
                StorageIdentifier.DEFAULT,
                it -> assertThat(it).extracting(this::str)
                    .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/private/files/")
            );
        assertThat(profile.getDocumentVersionStorage()).extracting(this::str)
            .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/versions/");
        assertThat(profile.getInboxWithFullAccess()).extracting(this::str)
            .isEqualTo("s3://adorsys-docusafe/datasafe/users/qqqq/public/inbox/");
        assertThat(profile.getAssociatedResources()).extracting(this::str)
            .containsExactly("s3://adorsys-docusafe/datasafe/users/qqqq/");
        assertThat(profile.getPublishPublicKeysTo()).isNull();
        assertThat(profile.getStorageCredentialsKeystore()).isNull();
    }

    private String str(AbsoluteLocation location) {
        return  location.location().asString();
    }
}
