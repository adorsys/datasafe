package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;
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
    private final DFSConnectionService dfsConnectionService;
    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPrivateKeyServiceImpl(DFSSystem dfsSystem, DFSConnectionService dfsConnectionService,
                                    BucketAccessService bucketAccessService) {
        this.dfsSystem = dfsSystem;
        this.dfsConnectionService = dfsConnectionService;
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public KeyStoreAccess keystore(UserIDAuth forUser) {
        DFSAccess access = bucketAccessService.privateAccessFor(
            forUser,
            profile -> profile.privateProfile(forUser).getKeystore()
        );

        DFSConnection connection = dfsConnectionService.obtain(access);
        KeyStoreAuth privateAuth = dfsSystem.privateKeyStoreAuth(forUser);

        Payload payload = connection.getBlob(access.getPhysicalPath());

        KeyStore keyStore = KeyStoreServiceImplBaseFunctions.loadKeyStore(
            payload.getData(),
            forUser.getUserID().getValue(),
            KeyStoreType.DEFAULT,
            new PasswordCallbackHandler(privateAuth.getReadStorePassword().getValue().toCharArray())
        );

        return new KeyStoreAccess(keyStore, dfsSystem.privateKeyStoreAuth(forUser));
    }
}
