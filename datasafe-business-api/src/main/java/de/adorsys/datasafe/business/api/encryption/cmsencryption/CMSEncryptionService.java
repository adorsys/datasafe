package de.adorsys.datasafe.business.api.encryption.cmsencryption;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSEncryptionService {

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID);
    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, PublicKey publicKey, KeyID publicKeyID, ASN1ObjectIdentifier algorithm);

    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID);
    OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID, ASN1ObjectIdentifier algorithm);
    
    InputStream buildDecryptionInputStream(InputStream inputStream, KeyStoreAccess keyStoreAccess);
}