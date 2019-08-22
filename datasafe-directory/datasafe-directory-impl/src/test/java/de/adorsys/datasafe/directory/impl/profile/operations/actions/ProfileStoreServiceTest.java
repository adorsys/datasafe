package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.OutputStream;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileStoreServiceTest extends BaseMockitoTest {

    private static final String PROFILE_STR = "profile";

    @Mock
    private AbsoluteLocation privateProfile;

    @Mock
    private AbsoluteLocation publicProfile;

    @Mock
    private GsonSerde serde;

    @Mock
    private UserProfileCache profileCache;

    @Mock
    private DFSConfig dfsConfig;

    @Mock
    private BucketAccessService access;

    @Mock
    private StorageWriteService writeService;

    @Mock
    private Map<UserID, UserPrivateProfile> cachePrivate;

    @Mock
    private Map<UserID, UserPublicProfile> cachePublic;

    @InjectMocks
    private ProfileStoreService tested;

    @Mock
    private UserID userID;

    @Mock
    private UserPrivateProfile profilePriv;

    @Mock
    private UserPublicProfile profilePub;

    @Mock
    private OutputStream os;

    @BeforeEach
    void init() {
        when(serde.toJson(profilePriv)).thenReturn(PROFILE_STR);
        when(serde.toJson(profilePub)).thenReturn(PROFILE_STR);
        when(writeService.write(any())).thenReturn(os);
        when(dfsConfig.privateProfile(userID)).thenReturn(privateProfile);
        when(dfsConfig.publicProfile(userID)).thenReturn(publicProfile);
        when(profileCache.getPrivateProfile()).thenReturn(cachePrivate);
        when(profileCache.getPublicProfile()).thenReturn(cachePublic);
    }

    @Test
    @SneakyThrows
    void registerPrivate() {
        tested.registerPrivate(userID, profilePriv);

        verify(serde).toJson(profilePriv);
        verify(os).write(PROFILE_STR.getBytes());
        verify(access).withSystemAccess(privateProfile);
        verify(profileCache).getPrivateProfile();
        verify(cachePrivate).remove(userID);
    }

    @Test
    @SneakyThrows
    void registerPublic() {
        tested.registerPublic(userID, profilePub);

        verify(serde).toJson(profilePub);
        verify(os).write(PROFILE_STR.getBytes());
        verify(access).withSystemAccess(publicProfile);
        verify(profileCache).getPublicProfile();
        verify(cachePublic).remove(userID);
    }
}
