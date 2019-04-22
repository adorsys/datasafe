package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.directory.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.document.DocumentReadService;
import de.adorsys.datasafe.business.api.storage.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.storage.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;

import javax.inject.Inject;
import java.security.KeyStore;

// DEPLOYMENT
/**
 * Retrieves and opens private keystore associated with user from DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final DFSSystem dfsSystem;
    private final BucketAccessService bucketAccessService;
    private final ProfileRetrievalService profile;
    private final DocumentReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(DFSSystem dfsSystem,
                                    BucketAccessService bucketAccessService, ProfileRetrievalService profile) {
        this.dfsSystem = dfsSystem;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
    }

    @Override
    public KeyStoreAccess keystore(UserIDAuth forUser) {
        PrivateResource access = bucketAccessService.privateAccessFor(
            forUser,
            profile.privateProfile(forUser).getKeystore()
        );

        Payload payload = readService.read(access);

        KeyStore keyStore = KeyStoreServiceImplBaseFunctions.loadKeyStore(
            payload.getData(),
            forUser.getUserID().getValue(),
            KeyStoreType.DEFAULT,
            new PasswordCallbackHandler(dfsSystem.systemDfs().getReadStorePassword().getValue().toCharArray())
        );

        return new KeyStoreAccess(keyStore, dfsSystem.privateKeyStoreAuth(forUser));
    }
}
