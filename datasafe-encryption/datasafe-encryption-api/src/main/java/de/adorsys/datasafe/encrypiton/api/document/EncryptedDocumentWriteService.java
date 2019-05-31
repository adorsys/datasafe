package de.adorsys.datasafe.encrypiton.api.document;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import java.io.OutputStream;

/**
 * Encrypted document write operation.
 */
public interface EncryptedDocumentWriteService {

    /**
     * Writes and encrypts data using public key cryptography, so that only private key owner can read it.
     * @param location Where to write data
     * @param publicKey Public key and its ID to encrypt with
     * @return Sink where you can send unencrypted data that will be encrypted and stored
     */
    OutputStream write(AbsoluteLocation<PublicResource> location, PublicKeyIDWithPublicKey publicKey);

    /**
     * Writes and encrypts data using symmetric cryptography.
     * @param location Where to write data
     * @param secretKey Secret key and its ID to encrypt with
     * @return Sink where you can send unencrypted data that will be encrypted and stored
     */
    OutputStream write(AbsoluteLocation<PrivateResource> location, SecretKeyIDWithKey secretKey);
}


