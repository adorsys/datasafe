package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import com.google.common.collect.Streams;
import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.exceptions.UserNotFoundException;
import de.adorsys.datasafe.directory.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@RuntimeDelegate
public class ProfileRemovalServiceImpl implements ProfileRemovalService {

    private final PrivateKeyService privateKeyService;
    private final KeyStoreCache keyStoreCache;
    private final UserProfileCache profileCache;
    private final StorageListService listService;
    private final BucketAccessService access;
    private final DFSConfig dfsConfig;
    private final StorageRemoveService removeService;
    private final ProfileRetrievalService retrievalService;

    @Inject
    public ProfileRemovalServiceImpl(PrivateKeyService privateKeyService, KeyStoreCache keyStoreCache,
                                     UserProfileCache profileCache, StorageListService listService,
                                     BucketAccessService access, DFSConfig dfsConfig,
                                     StorageRemoveService removeService, ProfileRetrievalService retrievalService) {
        this.privateKeyService = privateKeyService;
        this.keyStoreCache = keyStoreCache;
        this.profileCache = profileCache;
        this.listService = listService;
        this.access = access;
        this.dfsConfig = dfsConfig;
        this.removeService = removeService;
        this.retrievalService = retrievalService;
    }

    /**
     * Removes users' public and private profile, keystore, public keys and INBOX and private files
     */
    @Override
    public void deregister(UserIDAuth userID) {
        if (!retrievalService.userExists(userID.getUserID())) {
            log.debug("User deregistation failed. User '{}' does not exist", userID);
            throw new UserNotFoundException("User not found: " + userID);
        }

        // NOP just check that user has access
        privateKeyService.validateUserHasAccessOrThrow(userID);

        UserPublicProfile publicProfile = retrievalService.publicProfile(userID.getUserID());
        UserPrivateProfile privateProfile = retrievalService.privateProfile(userID);

        privateProfile.getPrivateStorage().forEach((id, path) -> removeAllIn(userID, path));
        removeAllIn(userID, privateProfile.getInboxWithFullAccess());
        removeAllIn(userID, privateProfile.getDocumentVersionStorage());

        Streams.concat(
                Stream.of(
                        privateProfile.getKeystore(),
                        privateProfile.getInboxWithFullAccess(),
                        privateProfile.getDocumentVersionStorage(),
                        privateProfile.getStorageCredentialsKeystore()
                ),
                privateProfile.getPrivateStorage().values().stream()
        )
        .filter(Objects::nonNull)
        .map(AbsoluteLocation::getResource)
        .map(it -> access.privateAccessFor(userID, it)).forEach(removeService::remove);

        removeService.remove(access.withSystemAccess(publicProfile.getPublicKeys()));

        removeUserProfileFiles(userID.getUserID());

        // remove everything else associated with user - i.e. parent folder of profile
        privateProfile.getAssociatedResources().stream()
                .map(it -> access.privateAccessFor(userID, it.getResource()))
                .forEach(removeService::remove);

        cleanupProfileCache(userID.getUserID());
        keyStoreCache.remove(userID.getUserID());

        log.debug("Deregistered user {}", userID);
        userID.getReadKeyPassword().clear();
    }

    protected void removeUserProfileFiles(UserID forUser) {
        removeService.remove(access.withSystemAccess(dfsConfig.privateProfile(forUser)));
        removeService.remove(access.withSystemAccess(dfsConfig.publicProfile(forUser)));
    }

    protected void cleanupProfileCache(UserID forUser) {
        profileCache.getPrivateProfile().remove(forUser);
        profileCache.getPublicProfile().remove(forUser);
    }

    private void removeAllIn(UserIDAuth userID, AbsoluteLocation<PrivateResource> location) {
        if (null == location) {
            return;
        }

        listService.list(
                access.privateAccessFor(userID, location.getResource())
        ).forEach(removeService::remove);
    }
}
