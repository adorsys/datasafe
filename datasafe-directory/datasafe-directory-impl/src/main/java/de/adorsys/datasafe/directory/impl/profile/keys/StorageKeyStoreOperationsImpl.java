package de.adorsys.datasafe.directory.impl.profile.keys;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.StorageIdentifier;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.interfaces.PBEKey;
import javax.inject.Inject;
import java.security.KeyStore;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Provides different kinds of high-level keystore (that is for STORAGE-CREDENTIALS) operations.
 * It is expected that backing storage service can access storage credentials keystore.
 */
@Slf4j
@RuntimeDelegate
public class StorageKeyStoreOperationsImpl implements StorageKeyStoreOperations {

    private final GsonSerde gson;
    private final KeyStoreService keyStoreService;
    private final GenericKeystoreOperations genericOper;
    private final ProfileRetrievalService profile;
    private final BucketAccessService access;
    private final KeyStoreCache keystoreCache;

    @Inject
    public StorageKeyStoreOperationsImpl(GsonSerde gson, KeyStoreService keyStoreService,
                                         GenericKeystoreOperations genericOper, ProfileRetrievalService profile,
                                         BucketAccessService access, KeyStoreCache keystoreCache) {
        this.gson = gson;
        this.keyStoreService = keyStoreService;
        this.genericOper = genericOper;
        this.profile = profile;
        this.access = access;
        this.keystoreCache = keystoreCache;
    }

    /**
     * Tries to re-read keystore from storage if supplied password can't open cached keystore.
     */
    @Override
    @SneakyThrows
    public StorageCredentials getStorageCredentials(UserIDAuth forUser, StorageIdentifier id) {
        PBEKey key = (PBEKey) genericOper.getKey(() -> keyStore(forUser), forUser, id.getId());
        return deserialize(key.getPassword());
    }

    /**
     * Reads aliases from keystore associated with user.
     */
    @Override
    @SneakyThrows
    public Set<StorageIdentifier> readAliases(UserIDAuth forUser) {
        return genericOper.readAliases(keyStore(forUser)).stream()
                .map(StorageIdentifier::new)
                .collect(Collectors.toSet());
    }

    @Override
    public void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword) {
        if (null == storageKeystoreLocation(forUser)) {
            return;
        }

        log.debug("Updating users' '{}' storage keystore ReadKeyPassword", forUser.getUserID());
        AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);
        genericOper.updateReadKeyPassword(keyStore(forUser), location, forUser, newPassword);
    }

    @Override
    public void addStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId, StorageCredentials credentials) {
        modifyAndStoreKeystore(
                forUser,
                keyStoreAccess -> keyStoreService.addPasswordBasedSecretKey(
                        keyStoreAccess,
                        storageId.getId(),
                        serialize(credentials)
                )
        );
    }

    @Override
    public void removeStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId) {
        modifyAndStoreKeystore(
                forUser,
                keyStoreAccess -> keyStoreService.removeKey(
                        keyStoreAccess,
                        storageId.getId()
                )
        );
    }

    @Override
    public void invalidateCache(UserIDAuth forUser) {
        keystoreCache.getStorageAccess().remove(forUser.getUserID());
    }

    private void modifyAndStoreKeystore(UserIDAuth forUser, Consumer<KeyStoreAccess> keystoreModifier) {
        log.debug("Modifying users' '{}' keystore", forUser.getUserID());
        AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);
        KeyStoreAccess keystoreWithCreds = new KeyStoreAccess(
                genericOper.readKeyStore(forUser, location),
                genericOper.keystoreAuth(forUser)
        );

        keystoreModifier.accept(keystoreWithCreds);

        genericOper.writeKeystore(
                forUser.getUserID(),
                genericOper.keystoreAuth(forUser),
                location,
                keystoreWithCreds.getKeyStore()
        );

        keystoreCache.getStorageAccess().remove(forUser.getUserID());
    }

    private StorageCredentials deserialize(char[] data) {
        return gson.fromJson(new String(data), StorageCredentials.class);
    }

    private char[] serialize(StorageCredentials credentials) {
        return gson.toJson(credentials).toCharArray();
    }

    private AbsoluteLocation<PrivateResource> keystoreLocationWithAccess(UserIDAuth forUser) {
        AbsoluteLocation<PrivateResource> location = storageKeystoreLocation(forUser);

        if (null == location) {
            throw new IllegalStateException("Profile does not have associated storage keystore");
        }

        return this.access.privateAccessFor(
                forUser,
                location.getResource()
        );
    }

    private AbsoluteLocation<PrivateResource> storageKeystoreLocation(UserIDAuth forUser) {
        return profile.privateProfile(forUser).getStorageCredentialsKeystore();
    }

    private KeyStore keyStore(UserIDAuth forUser) {
        return keystoreCache.getStorageAccess().computeIfAbsent(
                forUser.getUserID(),
                userId -> {
                    AbsoluteLocation<PrivateResource> location = keystoreLocationWithAccess(forUser);
                    return genericOper.readKeyStore(forUser, location);
                }
        );
    }
}
