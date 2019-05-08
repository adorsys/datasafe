package de.adorsys.datasafe.business.impl.encryption.cmsencryption;

import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;

@UtilityClass
public class DatasafeCryptoAlgorithm {

    // Asymmetric algorithms
    public static final ASN1ObjectIdentifier  AES128_CBC      = NISTObjectIdentifiers.id_aes128_CBC.intern();
    public static final ASN1ObjectIdentifier  AES192_CBC      = NISTObjectIdentifiers.id_aes192_CBC.intern();
    public static final ASN1ObjectIdentifier  AES256_CBC      = NISTObjectIdentifiers.id_aes256_CBC.intern();
    public static final ASN1ObjectIdentifier  AES128_CCM      = NISTObjectIdentifiers.id_aes128_CCM.intern();
    public static final ASN1ObjectIdentifier  AES192_CCM      = NISTObjectIdentifiers.id_aes192_CCM.intern();
    public static final ASN1ObjectIdentifier  AES256_CCM      = NISTObjectIdentifiers.id_aes256_CCM.intern();
    public static final ASN1ObjectIdentifier  AES128_GCM      = NISTObjectIdentifiers.id_aes128_GCM.intern();
    public static final ASN1ObjectIdentifier  AES192_GCM      = NISTObjectIdentifiers.id_aes192_GCM.intern();
    public static final ASN1ObjectIdentifier  AES256_GCM      = NISTObjectIdentifiers.id_aes256_GCM.intern();
    public static final ASN1ObjectIdentifier  AES128_WRAP     = NISTObjectIdentifiers.id_aes128_wrap.intern();
    public static final ASN1ObjectIdentifier  AES192_WRAP     = NISTObjectIdentifiers.id_aes192_wrap.intern();
    public static final ASN1ObjectIdentifier  AES256_WRAP     = NISTObjectIdentifiers.id_aes256_wrap.intern();
}
