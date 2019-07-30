package de.adorsys.datasafe.directory.api.profile.keys;

import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.StorageIdentifier;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;

import java.util.Set;

/**
 * This class is responsible for accessing/updating/creating users' keystore that contains storage credentials.
 */
public interface StorageKeyStoreOperations {

    /**
     * Updates ReadKeyPassword for users' keystore. Clears ALL cached keystores.
     * @param forUser Keystore owner.
     * @param newPassword New ReadKeyStore password.
     */
    void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword);

    /**
     * Stores storage access key in keystore.
     * @param forUser Owner of this key
     * @param storageId Storage identifier
     * @param credentials Key to store in keystore
     */
    void addStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId, StorageCredentials credentials);

    /**
     * Read key from the keystore associated with user.
     * @param forUser keystore owner
     * @param storageId Storage identifier to read credentials for
     * @return Key from keystore.
     */
    StorageCredentials getStorageCredentials(UserIDAuth forUser, StorageIdentifier storageId);

    /**
     * Aliases of keys stored in keystore associated with user.
     * @param forUser keystore owner.
     * @return Key aliases from keystore.
     */
    Set<StorageIdentifier> readAliases(UserIDAuth forUser);
}
