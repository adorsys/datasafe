package de.adorsys.datasafe.simple.adapter.impl.profile;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.impl.profile.keys.KeyStoreCache;
import de.adorsys.datasafe.directory.impl.profile.operations.actions.ProfileRemovalServiceImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;

import javax.inject.Inject;

/**
 * This service cleans up all users' files except profile json files. It assumes that user profile files does not
 * exist.
 */
public class DFSRelativeProfileRemovalServiceImpl extends ProfileRemovalServiceImpl {

    @Inject
    public DFSRelativeProfileRemovalServiceImpl(
            PrivateKeyService privateKeyService,
            KeyStoreCache keyStoreCache,
            StorageListService listService,
            BucketAccessService access,
            DFSConfig dfsConfig,
            StorageRemoveService removeService,
            ProfileRetrievalService retrievalService) {
        super(privateKeyService, keyStoreCache, null, listService, access, dfsConfig, removeService,
                retrievalService);
    }

    @Override
    protected void removeUserProfileFiles(UserID forUser) {
        // NOP
    }

    @Override
    protected void cleanupProfileCache(UserID forUser) {
        // NOP
    }
}
