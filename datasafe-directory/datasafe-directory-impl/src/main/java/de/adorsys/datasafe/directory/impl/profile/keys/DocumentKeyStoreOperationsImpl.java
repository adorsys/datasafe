package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.List;
import java.util.Set;

/**
 * Provides different kinds of high-level keystore (that is for DOCUMENT storage) operations.
 */
@Slf4j
@RuntimeDelegate
public class DocumentKeyStoreOperationsImpl implements DocumentKeyStoreOperations {

    private final KeyCreationConfig config;
    private final GenericKeystoreOperations genericOper;
    private final DFSConfig dfsConfig;
    private final BucketAccessService access;
    private final ProfileRetrievalService profile;
    private final StorageWriteService writeService;
    private final KeyStoreCache keystoreCache;
    private final KeyStoreService keyStoreService;

    @Inject
    public DocumentKeyStoreOperationsImpl(
            KeyCreationConfig config,
            GenericKeystoreOperations genericOper,
            DFSConfig dfsConfig,
            BucketAccessService access,
            ProfileRetrievalService profile,
            StorageWriteService writeService,
            KeyStoreCache keystoreCache,
            KeyStoreService keyStoreService) {
        this.config = config;
        this.genericOper = genericOper;
        this.dfsConfig = dfsConfig;
        this.access = access;
        this.profile = profile;
        this.writeService = writeService;
        this.keystoreCache = keystoreCache;
        this.keyStoreService = keyStoreService;
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     */
    @Override
    @SneakyThrows
    public Key getKey(UserIDAuth forUser, String alias) {
        return genericOper.getKey(() -> keyStore(forUser), forUser, alias);
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @Override
    @SneakyThrows
    public Set<String> readAliases(UserIDAuth forUser) {
        return genericOper.readAliases(keyStore(forUser));
    }

    @Override
    @SneakyThrows
    public List<PublicKeyIDWithPublicKey> createAndWriteKeyStore(UserIDAuth forUser) {
        KeyStoreAuth auth = keystoreAuth(forUser, forUser.getReadKeyPassword());
        KeyStore keystoreBlob = keyStoreService.createKeyStore(
                auth,
                config
        );

        writeKeystore(forUser.getUserID(), auth, keystoreLocationWithAccess(forUser), keystoreBlob);
        return keyStoreService.getPublicKeys(new KeyStoreAccess(keystoreBlob, auth));
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        log.debug("Updating users' '{}' document keystore ReadKeyPassword", forUser.getUserID());
        AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);
        genericOper.updateReadKeyPassword(keyStore(forUser), location, forUser, newPassword);
    }

    private AbsoluteLocation<PrivateResource> keystoreLocationWithAccess(UserIDAuth forUser) {
        return this.access.privateAccessFor(
                forUser,
                profile.privateProfile(forUser).getKeystore().getResource()
        );
    }

    private KeyStoreAuth keystoreAuth(UserIDAuth forUser, ReadKeyPassword readKeyPassword) {
        ReadStorePassword readStorePassword = dfsConfig.privateKeyStoreAuth(forUser).getReadStorePassword();

        return new KeyStoreAuth(readStorePassword, readKeyPassword);
    }

    @SneakyThrows
    private <T extends ResourceLocation<T>> void writeKeystore(UserID forUser, KeyStoreAuth auth,
                                                               AbsoluteLocation<T> keystore, KeyStore keystoreBlob) {
        try (OutputStream os = writeService.write(WithCallback.noCallback(access.withSystemAccess(keystore)))) {
            os.write(keyStoreService.serialize(keystoreBlob, auth.getReadStorePassword()));
        }
        log.debug("Keystore created for user {} in path {}", forUser, keystore);
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getKeystore().computeIfAbsent(
                forUser,
                userId -> {
                    AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);
                    return genericOper.readKeyStore(forUser, location);
                }
        );
    }
}
