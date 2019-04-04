package de.adorsys.docusafe2.business.api.cmsencryption;

import de.adorsys.docusafe2.business.api.cmsencryption.types.DocumentContent;
import org.bouncycastle.cms.CMSEnvelopedData;

import java.security.KeyStore;
import java.security.PublicKey;

public interface CMSEncryptionService {

    CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, byte[] publicKeyID);

    DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStore keyStore, char[] keyStorePass);
}