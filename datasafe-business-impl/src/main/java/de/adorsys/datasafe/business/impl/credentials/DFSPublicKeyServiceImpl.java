package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.deployment.keystore.PublicKeyService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.deployment.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;

import javax.inject.Inject;
import java.security.KeyStore;
import java.util.List;

// DEPLOYMENT
/**
 * Retrieves and opens public keystore associated with user from DFS storage.
 */
public class DFSPublicKeyServiceImpl implements PublicKeyService {

    private final DFSSystem dfsSystem;
    private final KeyStoreService keyStoreService;
    private final DFSConnectionService dfsConnectionService;
    private final BucketAccessService bucketAccessService;

    @Inject
    public DFSPublicKeyServiceImpl(DFSSystem dfsSystem, KeyStoreService keyStoreService,
                                   DFSConnectionService dfsConnectionService, BucketAccessService bucketAccessService) {
        this.dfsSystem = dfsSystem;
        this.keyStoreService = keyStoreService;
        this.dfsConnectionService = dfsConnectionService;
        this.bucketAccessService = bucketAccessService;
    }

    @Override
    public PublicKeyIDWithPublicKey publicKey(UserID forUser) {
        DFSAccess access = bucketAccessService.publicAccessFor(
            forUser,
            profile -> profile.publicProfile(forUser).getPublicKeys()
        );

        DFSConnection connection = dfsConnectionService.obtain(access);
        KeyStoreAuth publicAuth = dfsSystem.publicKeyStoreAuth();

        Payload payload = connection.getBlob(access.getPhysicalPath());

        KeyStore keyStore = KeyStoreServiceImplBaseFunctions.loadKeyStore(
            payload.getData(),
            forUser.getValue(),
            KeyStoreType.DEFAULT,
            new PasswordCallbackHandler(publicAuth.getReadStorePassword().getValue().toCharArray())
        );

        List<PublicKeyIDWithPublicKey> publicKeyList = keyStoreService.getPublicKeys(
            new KeyStoreAccess(
                keyStore,
                publicAuth
            )
        );

        return publicKeyList.get(0);
    }
}
