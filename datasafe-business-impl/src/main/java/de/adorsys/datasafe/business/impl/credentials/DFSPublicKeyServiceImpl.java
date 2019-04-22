package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.directory.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.storage.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;

import javax.inject.Inject;
import java.net.URI;
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
    private final ProfileRetrievalService profiles;

    @Inject
    public DFSPublicKeyServiceImpl(DFSSystem dfsSystem, KeyStoreService keyStoreService,
                                   DFSConnectionService dfsConnectionService, BucketAccessService bucketAccessService,
                                   ProfileRetrievalService profiles) {
        this.dfsSystem = dfsSystem;
        this.keyStoreService = keyStoreService;
        this.dfsConnectionService = dfsConnectionService;
        this.bucketAccessService = bucketAccessService;
        this.profiles = profiles;
    }

    @Override
    public PublicKeyIDWithPublicKey publicKey(UserID forUser) {
        URI accessiblePublicKey = bucketAccessService.publicAccessFor(
            forUser,
            profiles.publicProfile(forUser).getPublicKeys()
        );

        DFSConnection connection = dfsConnectionService.obtain(accessiblePublicKey);
        KeyStoreAuth publicAuth = dfsSystem.publicKeyStoreAuth();

        Payload payload = connection.getBlob(new BucketPath(accessiblePublicKey.getPhysicalPath().toString()));

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
