package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DFSRelativeProfileRetrievalServiceImpl extends ProfileRetrievalServiceImpl {

    DFSRelativeProfileRetrievalServiceImpl(DFSConfig dfsConfig, StorageReadService readService,
                                                  StorageCheckService checkService, BucketAccessService access,
                                                  GsonSerde serde, UserProfileCache userProfileCache) {
        super(dfsConfig, readService, checkService, access, serde, userProfileCache);
    }

    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        CreateUserPublicProfile createUserPublicProfile = dfsConfig.defaultPublicTemplate(new UserIDAuth(ofUser, new ReadKeyPassword("")));
        UserPublicProfile userPublicProfile = createUserPublicProfile.removeAccess();
        log.debug("get public profile {} for user {}", userPublicProfile, ofUser);
        return userPublicProfile;
    }

    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {

        CreateUserPrivateProfile privateProfile = dfsConfig.defaultPrivateTemplate(ofUser);
        UserPrivateProfile userPrivateProfile = privateProfile.removeAccess();

        log.debug("get private profile {} for user {}", userPrivateProfile, ofUser);
        return userPrivateProfile;
    }

    @Override
    public boolean userExists(UserID ofUser) {
        return super.userExists(ofUser);
    }
}
