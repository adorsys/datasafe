package de.adorsys.datasafe.encrypiton.api.keystore;

import de.adorsys.datasafe.encrypiton.api.types.keystore.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TODO: Move it into another subproject - it is very heavyweight and used mostly in profile module.
/**
 * This class is responsible for creating,serializing keystores as well as reading keys from keystores but works
 * with concrete keystore blob.
 */
public interface KeyStoreService {

    /**
     * Creates keystore.
     * @param keyStoreAuth Keys for opening keystore and reading key from it
     * @param keyStoreType Keystore type, example: PKCS or PKCS12 or JKS
     * @param config Keystore will be pre-populated with keys according to it
     * @return Built keystore that is ready to use
     */
    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config);

    /**
     * Creates keystore that has additional secret keys in it.
     * @param keyStoreAuth Keys for opening keystore and reading key from it
     * @param keyStoreType Keystore type, example: PKCS12 or UBER or JKS
     * @param config Keystore will be pre-populated with keys according to it
     * @param secretKeys Secret keys to store in keystore, if value is empty - key will be generated
     * @return Built keystore that is ready to use
     */
    KeyStore createKeyStore(KeyStoreAuth keyStoreAuth,
                            KeyStoreType keyStoreType,
                            KeyStoreCreationConfig config,
                            Map<KeyID, Optional<SecretKeyEntry>> secretKeys);

    /**
     * Updates keystore access credentials and returns new keystore with new credentials.
     * @param current Original keystore
     * @param currentCredentials Oriignal keystore credentials
     * @param newCredentials New credentials to use
     * @return Cloned old keystore that can be opened using new credentials only.
     */
    KeyStore updateKeyStoreReadKeyPassword(KeyStore current,
                                           KeyStoreAuth currentCredentials,
                                           KeyStoreAuth newCredentials);

    /**
     * Reads public keys from the keystore.
     * @param keyStoreAccess Key to open keystore (only {@link KeyStoreAuth#getReadStorePassword()} is used)
     * @return List of public keys within the keystore
     */
    List<PublicKeyEntry> getPublicKeys(KeyStoreAccess keyStoreAccess);

    /**
     * Reads private key from the keystore.
     * @param keyStoreAccess Key to open keystore and read key, (both
     * {@link KeyStoreAuth#getReadStorePassword()} and {@link KeyStoreAuth#getReadKeyPassword()} are used)
     * @param keyID Private key ID to read
     * @return Private key associated with given ID
     */
    PrivateKey getPrivateKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    /**
     * Reads secret key from the keystore.
     * @param keyStoreAccess Key to open keystore and read key, (both
     * {@link KeyStoreAuth#getReadStorePassword()} and {@link KeyStoreAuth#getReadKeyPassword()} are used)
     * @param keyID Secret key ID to read
     * @return Secret key associated with given ID
     */
    SecretKeySpec getSecretKey(KeyStoreAccess keyStoreAccess, KeyID keyID);

    /**
     * Adds password-like secret key to keystore.
     * @param keyStoreAccess Keystore with its access details
     * @param alias Key alias to add
     * @param secretToStore Key value to store in keystore (in {@code keyStoreAccess})
     */
    void addPasswordBasedSecretKey(KeyStoreAccess keyStoreAccess, String alias, char[] secretToStore);

    /**
     * Removes key that is identified by {@code alias} from keystore.
     * @param keyStoreAccess Keystore with its access details
     * @param alias Key alias to remove
     */
    void removeKey(KeyStoreAccess keyStoreAccess, String alias);

    /**
     * Converts keystore into bytes, they are safe to be store/transferred because of encryption using
     * {@link KeyStoreAuth#getReadStorePassword()}
     * @param store Keystore that will be serialized
     * @param storeId Assign ID to serialized keystore
     * @param password Encrypts byte sequence
     * @return Encrypted serialized keystore
     */
    byte[] serialize(KeyStore store, String storeId, ReadStorePassword password);

    /**
     * Reads encrypted keystore from its byte representation - decryption is done using
     * {@link KeyStoreAuth#getReadStorePassword()}
     * @param payload Bytes to read from
     * @param storeId Specifies which keystore to read from bytes
     * @param password Decrypts byte sequence
     * @return Decrypted keystore
     */
    KeyStore deserialize(byte[] payload, String storeId, ReadStorePassword password);
}
