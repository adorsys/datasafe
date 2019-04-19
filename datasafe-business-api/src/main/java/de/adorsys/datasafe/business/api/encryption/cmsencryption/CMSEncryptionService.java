package de.adorsys.datasafe.business.api.encryption.cmsencryption;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;

public interface CMSEncryptionService {

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID);
    InputStream buildDecryptionInputStream(InputStream inputStream, KeyStoreAccess keyStoreAccess);
}