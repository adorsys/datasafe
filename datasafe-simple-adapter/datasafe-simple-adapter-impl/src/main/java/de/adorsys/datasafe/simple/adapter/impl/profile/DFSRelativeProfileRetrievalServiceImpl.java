package de.adorsys.datasafe.simple.adapter.impl.profile;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRetrievalServiceImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageCheckService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This service ignores profiles stored at some external location and instead assumes that all files are relative
 * to system root.
 */
@Slf4j
public class DFSRelativeProfileRetrievalServiceImpl extends ProfileRetrievalServiceImpl {

    private final DFSConfig dfsConfig;
    private final StorageCheckService checkService;
    private final BucketAccessService access;

    @Inject
    public DFSRelativeProfileRetrievalServiceImpl(DFSConfig dfsConfig, StorageCheckService checkService,
                                           BucketAccessService access) {
        super(null, null, null, null, null, null);

        this.dfsConfig = dfsConfig;
        this.checkService = checkService;
        this.access = access;
    }

    @Override
    public UserPublicProfile publicProfile(UserID ofUser) {
        CreateUserPublicProfile createUserPublicProfile = dfsConfig.defaultPublicTemplate(ofUser);
        UserPublicProfile userPublicProfile = createUserPublicProfile.buildPublicProfile();
        log.debug("get public profile {} for user {}", userPublicProfile, ofUser);
        return userPublicProfile;
    }

    @Override
    public UserPrivateProfile privateProfile(UserIDAuth ofUser) {

        CreateUserPrivateProfile privateProfile = dfsConfig.defaultPrivateTemplate(ofUser);
        UserPrivateProfile userPrivateProfile = privateProfile.buildPrivateProfile();

        log.debug("get private profile {} for user {}", userPrivateProfile, ofUser);
        return userPrivateProfile;
    }

    @Override
    public boolean userExists(UserID ofUser) {
        return checkService.objectExists(
                access.withSystemAccess(dfsConfig.defaultPublicTemplate(ofUser).getPublicKeys())
        );
    }
}
