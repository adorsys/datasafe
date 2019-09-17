package de.adorsys.datasafe.simple.adapter.impl.legacy.directory;

import de.adorsys.datasafe.directory.api.profile.keys.DocumentKeyStoreOperations;
import de.adorsys.datasafe.directory.impl.profile.keys.DFSPrivateKeyServiceImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;

import javax.inject.Inject;

import static de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreCreationConfig.PATH_KEY_ID_PREFIX;

/**
 * Retrieves and opens private keystore associated with user location DFS storage.
 * Attempts to re-read keystore if not able to open it.
 */
public class LegacyDFSPrivateKeyServiceImpl extends DFSPrivateKeyServiceImpl {

    @Inject
    public LegacyDFSPrivateKeyServiceImpl(DocumentKeyStoreOperations keyStoreOper) {
        super(keyStoreOper);
    }

    /**
     * Reads path encryption secret key from DFS and caches the result.
     */
    @Override
    public PathEncryptionSecretKey pathEncryptionSecretKey(UserIDAuth forUser) {
        SecretKeyIDWithKey secretKeyIDWithKey = keyByPrefix(forUser, PATH_KEY_ID_PREFIX);
        return new PathEncryptionSecretKey(
                secretKeyIDWithKey.getKeyID(),
                secretKeyIDWithKey.getSecretKey(),
                secretKeyIDWithKey.getKeyID(),
                secretKeyIDWithKey.getSecretKey()
        );
    }
}
