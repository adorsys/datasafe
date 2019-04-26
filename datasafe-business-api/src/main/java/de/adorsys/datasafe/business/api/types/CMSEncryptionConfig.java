package de.adorsys.datasafe.business.api.types;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSEncryptionConfig {

    void setAlgorithm(ASN1ObjectIdentifier algorithm);
    ASN1ObjectIdentifier getAlgorithm();

}
