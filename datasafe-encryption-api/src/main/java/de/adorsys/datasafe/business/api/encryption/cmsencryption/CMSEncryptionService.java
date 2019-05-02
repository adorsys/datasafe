package de.adorsys.datasafe.business.api.encryption.cmsencryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import de.adorsys.datasafe.business.api.types.keystore.KeyID;
import de.adorsys.datasafe.business.api.types.keystore.KeyStoreAccess;


public interface CMSEncryptionService {

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID);

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID);

    InputStream buildDecryptionInputStream(InputStream inputStream, KeyStoreAccess keyStoreAccess);
}