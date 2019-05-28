package de.adorsys.datasafe.encrypiton.api.document;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import java.io.OutputStream;

/**
 * File write operation at a given location.
 */
public interface EncryptedDocumentWriteService {

    OutputStream write(AbsoluteLocation<PublicResource> location, PublicKeyIDWithPublicKey publicKey);
    OutputStream write(AbsoluteLocation<PrivateResource> location, SecretKeyIDWithKey secretKey);
}


