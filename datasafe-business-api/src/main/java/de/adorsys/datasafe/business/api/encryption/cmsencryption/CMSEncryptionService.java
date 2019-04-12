package de.adorsys.datasafe.business.api.encryption.cmsencryption;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import org.bouncycastle.cms.CMSEnvelopedData;

import java.security.PublicKey;

public interface CMSEncryptionService {

    CMSEnvelopedData encrypt(DocumentContent data, PublicKey publicKey, KeyID publicKeyID);

    DocumentContent decrypt(CMSEnvelopedData cmsEnvelopedData, KeyStoreAccess keyStoreAccess);
}