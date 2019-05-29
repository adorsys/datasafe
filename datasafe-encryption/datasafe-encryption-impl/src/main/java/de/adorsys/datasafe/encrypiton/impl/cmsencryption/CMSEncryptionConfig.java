package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Provides algorithm ID used for CMS-encryption.
 */
public interface CMSEncryptionConfig {

    /**
     * BouncyCastle compatible algorithm identifier.
     */
    ASN1ObjectIdentifier getAlgorithm();
}
