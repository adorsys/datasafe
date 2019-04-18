package de.adorsys.datasafe.business.api.encryption;

import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines an interface to encrypt, sign, decrypt and verify data streams.
 *
 * @author fpo
 */
public interface EncryptionService {

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKeyIDWithPublicKey publicKeyIdWithPublicKey);

    InputStream buildDecryptionInputStream(InputStream inputStream, KeyStoreAccess keyStoreAccess);

}