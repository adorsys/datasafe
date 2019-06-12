package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRemovalService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.impl.profile.exceptions.UserNotFoundException;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
@RuntimeDelegate
public class ProfileRemovalServiceImpl implements ProfileRemovalService {

    private final StorageListService listService;
    private final DFSConfig dfsConfig;
    private final StorageRemoveService removeService;
    private final ProfileRetrievalService retrievalService;

    @Inject
    public ProfileRemovalServiceImpl(StorageListService listService, DFSConfig dfsConfig,
                                     StorageRemoveService removeService, ProfileRetrievalService retrievalService) {
        this.listService = listService;
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

        AbsoluteLocation<PrivateResource> privateStorage = retrievalService.privateProfile(userID).getPrivateStorage();
        removeStorageByLocation(userID, privateStorage);

        AbsoluteLocation<PrivateResource> inbox = retrievalService.privateProfile(userID).getInboxWithFullAccess();
        removeStorageByLocation(userID, inbox);

        removeService.remove(retrievalService.privateProfile(userID).getKeystore());
        removeService.remove(privateStorage);
        removeService.remove(inbox);
        removeService.remove(dfsConfig.privateProfile(userID.getUserID()));
        removeService.remove(dfsConfig.publicProfile(userID.getUserID()));
        log.debug("Deregistered user {}", userID);
    }

    private void removeStorageByLocation(UserIDAuth userID, AbsoluteLocation<PrivateResource> privateStorage) {
        listService.list(new ListRequest<>(userID, privateStorage).getLocation())
                .forEach(removeService::remove);
    }
}
