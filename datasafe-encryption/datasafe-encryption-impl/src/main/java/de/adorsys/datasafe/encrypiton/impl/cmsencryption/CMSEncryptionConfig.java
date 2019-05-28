package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMSEncryptionConfig {

    ASN1ObjectIdentifier getAlgorithm();
}
