package de.adorsys.datasafe.business.api.types;

import lombok.AllArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@AllArgsConstructor
public class CMSEncryptionConfig {

    private ASN1ObjectIdentifier cryptoAlgorithm;

    public ASN1ObjectIdentifier getAlgorithm() {
        return cryptoAlgorithm;
    }

}
