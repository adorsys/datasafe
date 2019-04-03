package de.adorsys.docusafe2.services;

import java.security.KeyStore;
import java.security.PublicKey;

public interface CMSEncryptionService {

    byte[] encrypt(byte[] data, PublicKey publicKey, byte[] publicKeyID);

    byte[] decrypt(byte[] encryptedData, KeyStore keyStore, char[] keyStorePass);
}