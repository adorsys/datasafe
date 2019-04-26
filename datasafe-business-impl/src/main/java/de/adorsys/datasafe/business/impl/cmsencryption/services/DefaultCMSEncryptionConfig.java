package de.adorsys.datasafe.business.impl.cmsencryption.services;

import de.adorsys.datasafe.business.api.types.CMSEncryptionConfig;
import de.adorsys.datasafe.business.api.types.DatasafeCryptoAlgorithm;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import javax.inject.Inject;

public class DefaultCMSEncryptionConfig implements CMSEncryptionConfig {

    private ASN1ObjectIdentifier algorithm;

    @Inject
    public DefaultCMSEncryptionConfig() {
        algorithm = DatasafeCryptoAlgorithm.AES256_CBC;
    }

    @Override
    public void setAlgorithm(ASN1ObjectIdentifier algorithm) {
        throw new IllegalArgumentException("Default CMS Encryption configuration uses by default AES 256 CBC algorithm");
    }

    @Override
    public ASN1ObjectIdentifier getAlgorithm() {
        return algorithm;
    }
}
