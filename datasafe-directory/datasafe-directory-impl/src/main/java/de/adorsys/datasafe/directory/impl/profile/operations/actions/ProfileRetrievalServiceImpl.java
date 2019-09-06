package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.InputStream;

@Slf4j
@RuntimeDelegate
public class ProfileRetrievalServiceImpl implements ProfileRetrievalService {

    private final DFSConfig dfsConfig;
    private final StorageReadService readService;
    private final StorageCheckService checkService;
    private final BucketAccessService access;
    private final GsonSerde serde;
    private final UserProfileCache userProfileCache;

    @Inject
    public ProfileRetrievalServiceImpl(DFSConfig dfsConfig, StorageReadService readService,
                                       StorageCheckService checkService, BucketAccessService access, GsonSerde serde,
                                       UserProfileCache userProfileCache) {
        this.dfsConfig = dfsConfig;
        this.readService = readService;
        this.checkService = checkService;
        this.access = access;
        this.serde = serde;
        this.userProfileCache = userProfileCache;
    }

    /**
     * Reads user public profile from DFS, uses {@link UserProfileCache} for caching it
     */
    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        UserPublicProfile userPublicProfile = userProfileCache.getPublicProfile().computeIfAbsent(
                ofUser,
                id -> readProfile(dfsConfig.publicProfile(ofUser), UserPublicProfile.class)
        );

        log.debug("get public profile {} for user {}", userPublicProfile, ofUser);
        return userPublicProfile;
    }

    /**
     * Reads user private profile from DFS, uses {@link UserProfileCache} for caching it
     */
    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {
        UserPrivateProfile userPrivateProfile = userProfileCache.getPrivateProfile().computeIfAbsent(
                ofUser.getUserID(),
                id -> readProfile(dfsConfig.privateProfile(ofUser.getUserID()), UserPrivateProfile.class)
        );
        log.debug("get private profile {} for user {}", userPrivateProfile, ofUser);
        return userPrivateProfile;
    }

    /**
     * Checks if user exists by validating that his both public and private profile files do exist.
     */
    @Override
    public boolean userExists(UserID ofUser) {
        return checkService.objectExists(access.withSystemAccess(dfsConfig.privateProfile(ofUser))) &&
                checkService.objectExists(access.withSystemAccess(dfsConfig.publicProfile((ofUser))));
    }

    @SneakyThrows
    private <T> T readProfile(AbsoluteLocation resource, Class<T> clazz) {
        try (InputStream is = readService.read(access.withSystemAccess(resource))) {
            log.debug("read profile {}", resource.location());
            return serde.fromJson(new String(ByteStreams.toByteArray(is)), clazz);
        }
    }
}
