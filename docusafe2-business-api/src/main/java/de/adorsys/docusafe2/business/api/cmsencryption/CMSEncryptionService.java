package de.adorsys.docusafe2.business.api.cmsencryption;

import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.docusafe2.business.api.types.DocumentContent;
import de.adorsys.docusafe2.business.api.keystore.types.KeyID;
import de.adorsys.docusafe2.business.api.keystore.types.ReadStorePassword;
import org.bouncycastle.cms.CMSEnvelopedData;

import java.security.KeyStore;
import java.security.PublicKey;

public interface CMSEncryptionService {

    CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, KeyID publicKeyID);

    DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStoreAccess keyStoreAccess);
}