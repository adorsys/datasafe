package de.adorsys.datasafe.encrypiton.api.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


/**
 * Interface for performing content-stream encryption and decryption.
 */
public interface CMSEncryptionService {

    /**
     * Builds asymmetrically encrypted stream using public-key cryptography.
     * @param dataContentStream Stream to encrypt
     * @param publicKeys Contains user public key and
     *        public-key ID which gets embedded into a stream, used for finding the private key to decrypt
     * @return Encrypted stream that wraps {@code dataContentStream}
     * @apiNote Closes underlying stream when result is closed
     */
    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, Set<PublicKeyIDWithPublicKey> publicKeys);

    /**
     * Builds symmetrically encrypted stream.
     * @param dataContentStream Stream to encrypt
     * @param secretKey User secret key
     * @param secretKeyID User key ID gets embedded into a stream, used for finding the key to decrypt
     * @return Encrypted stream that wraps {@code dataContentStream}
     * @apiNote Closes underlying stream when result is closed
     */
    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID);

    /**
     * Builds decrypted stream out of encrypted one.
     * @param inputStream Stream to decrypt
     * @param keysByIds Key to its ID mapping function - you request for the set of key aliases and function returns
     * only those that were found as a map (key id - key), will retrieve key for decryption using this. Implemented
     * as a request once for many instead of iterating one-by-one to avoid possible concurrency issues.
     * @return Decrypted stream that wraps {@code inputStream}
     * @apiNote Closes underlying stream when result is closed
     */
    InputStream buildDecryptionInputStream(InputStream inputStream, Function<Set<String>, Map<String, Key>> keysByIds);
}
