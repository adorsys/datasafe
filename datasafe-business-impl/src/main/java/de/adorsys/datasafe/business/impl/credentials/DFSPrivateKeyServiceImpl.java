package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.directory.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreType;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.keystore.generator.KeyStoreServiceImplBaseFunctions;
import de.adorsys.datasafe.business.impl.keystore.generator.PasswordCallbackHandler;
import de.adorsys.datasafe.business.impl.profile.DFSSystem;

import javax.inject.Inject;
import java.security.KeyStore;

// DEPLOYMENT
/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 */
public class DFSPrivateKeyServiceImpl implements PrivateKeyService {

    private final DFSSystem dfsSystem;
    private final BucketAccessServiceImpl bucketAccessService;
    private final ProfileRetrievalService profile;
    private final StreamReadUtil streamReadUtil;
    private final StorageReadService readService;

    @Inject
    public DFSPrivateKeyServiceImpl(DFSSystem dfsSystem, BucketAccessServiceImpl bucketAccessService,
                                    ProfileRetrievalService profile, StreamReadUtil streamReadUtil,
                                    StorageReadService readService) {
        this.dfsSystem = dfsSystem;
        this.bucketAccessService = bucketAccessService;
        this.profile = profile;
        this.streamReadUtil = streamReadUtil;
        this.readService = readService;
    }

    @Override
    public KeyStoreAccess keystore(UserIDAuth forUser) {
        PrivateResource access = bucketAccessService.privateAccessFor(
            forUser,
            profile.privateProfile(forUser).getKeystore()
        );

        byte[] payload = streamReadUtil.readStream(readService.read(access));

        KeyStore keyStore = KeyStoreServiceImplBaseFunctions.loadKeyStore(
            payload,
            forUser.getUserID().getValue(),
            KeyStoreType.DEFAULT,
            new PasswordCallbackHandler(dfsSystem.systemKeystoreAuth().toCharArray())
        );

        return new KeyStoreAccess(keyStore, dfsSystem.privateKeyStoreAuth(forUser));
    }
}
