package de.adorsys.datasafe.business.impl.encryption.cmsencryption;

import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import javax.inject.Inject;

@Getter
public class DefaultCMSEncryptionConfig implements CMSEncryptionConfig {

    private final ASN1ObjectIdentifier algorithm;

    @Inject
    public DefaultCMSEncryptionConfig() {
        algorithm = DatasafeCryptoAlgorithm.AES256_CBC;
    }
}
