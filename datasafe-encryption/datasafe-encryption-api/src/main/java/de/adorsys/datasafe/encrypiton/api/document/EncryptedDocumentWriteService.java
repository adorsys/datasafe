package de.adorsys.datasafe.encrypiton.api.document;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;

import java.io.OutputStream;
import java.util.Map;

/**
 * Encrypted document write operation.
 */
public interface EncryptedDocumentWriteService {

    /**
     * Writes and encrypts data using public key cryptography, so that only private key owner can read it.
     * Supports sharing with multiple users, so that encrypted copy will be written to each recipient and
     * he can read it using his private key.
     * @param recipientsWithInbox Map of (recipient public key - recipients' inbox) of users with whom to share file.
     * @return Sink where you can send unencrypted data that will be encrypted and stored
     */
    OutputStream write(Map<PublicKeyIDWithPublicKey, AbsoluteLocation> recipientsWithInbox);

    /**
     * Writes and encrypts data using symmetric cryptography.
     * @param locationWithCallback Where to write data with callbacks to notify
     * @param secretKey Secret key and its ID to encrypt with
     * @return Sink where you can send unencrypted data that will be encrypted and stored
     */
    OutputStream write(
            WithCallback<AbsoluteLocation<PrivateResource>, ResourceWriteCallback> locationWithCallback,
            SecretKeyIDWithKey secretKey
    );
}


