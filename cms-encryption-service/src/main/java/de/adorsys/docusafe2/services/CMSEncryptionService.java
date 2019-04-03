package de.adorsys.docusafe2.services;

import de.adorsys.docusafe2.model.DocumentContent;
import org.bouncycastle.cms.CMSEnvelopedData;

import java.security.KeyStore;
import java.security.PublicKey;

public interface CMSEncryptionService {

    CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, byte[] publicKeyID);

    DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStore keyStore, char[] keyStorePass);
}