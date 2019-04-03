package de.adorsys.docusafe2.services;

import de.adorsys.docusafe2.keystore.api.types.KeyID;
import de.adorsys.docusafe2.keystore.api.types.KeyStoreAccess;

import java.security.PublicKey;

public interface CMSEncryptionService {

    byte[] encrypt(byte[] data, PublicKey publicKey, KeyID publicKeyID);

    byte[] decrypt(byte[] encryptedData, KeyStoreAccess keyStoreAccess);
}
