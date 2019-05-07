package de.adorsys.datasafe.business.api.encryption.cmsencryption;

import de.adorsys.datasafe.business.api.types.keystore.KeyID;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.PublicKey;
import java.util.function.Function;


public interface CMSEncryptionService {

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID);

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID);

    InputStream buildDecryptionInputStream(InputStream inputStream, Function<String, Key> keyById);
}