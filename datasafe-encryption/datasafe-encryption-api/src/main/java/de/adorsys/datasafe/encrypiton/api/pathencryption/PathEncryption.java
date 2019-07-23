package de.adorsys.datasafe.encrypiton.api.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.Uri;

import java.util.function.Function;

/**
 * Encrypts and decrypts relative URI's using users' path encryption key.
 */
public interface PathEncryption {

    /**
     * Encrypts relative URL using path encryption key.
     * @param forUser Credentials to access path encryption key
     * @param path Path to encrypt
     * @return Encrypted URL friendly path.
     */
    Uri encrypt(UserIDAuth forUser, Uri path);

    /**
     * Decrypts relative URL using path encryption key.
     * @param forUser Credentials to access path encryption key
     * @return Function to decrypt path with cached (locally as lambda capture) encryption key,
     * that when applied on encrypted path returns decrypted path.
     * Function: Encrypted URI -> Decrypted URI
     */
    Function<Uri, Uri> decryptor(UserIDAuth forUser);
}
