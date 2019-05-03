package de.adorsys.datasafe.business.impl.encryption.cmsencryption;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSEncryptionConfig {

    ASN1ObjectIdentifier getAlgorithm();
}
