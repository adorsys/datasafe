package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;

import java.io.OutputStream;

/**
 * File write operation at a given location.
 */
public interface EncryptedDocumentWriteService {

    OutputStream write(PublicResource location, PublicKeyIDWithPublicKey publicKey);
    OutputStream write(PrivateResource location, SecretKeyIDWithKey secretKey);
}


