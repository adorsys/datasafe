package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import com.google.common.collect.ImmutableMap;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import javax.inject.Inject;
import java.util.Map;

/**
 * Provides algorithm ID used for CMS-encryption.
 */
@RuntimeDelegate
public class ASNCmsEncryptionConfig {

    /**
     * These are recommended mappings, one can override this static map by RuntimeDelegate to
     * ASNCmsEncryptionConfig.
     */
    private static final Map<String, ASN1ObjectIdentifier> MAPPINGS =
            ImmutableMap.<String, ASN1ObjectIdentifier>builder()
                    .put("AES128_CBC", NISTObjectIdentifiers.id_aes128_CBC)
                    .put("AES192_CBC", NISTObjectIdentifiers.id_aes192_CBC)
                    .put("AES256_CBC", NISTObjectIdentifiers.id_aes256_CBC)
                    .put("AES128_CCM", NISTObjectIdentifiers.id_aes128_CCM)
                    .put("AES192_CCM", NISTObjectIdentifiers.id_aes192_CCM)
                    .put("AES256_CCM", NISTObjectIdentifiers.id_aes256_CCM)
                    .put("AES128_GCM", NISTObjectIdentifiers.id_aes128_GCM)
                    .put("AES192_GCM", NISTObjectIdentifiers.id_aes192_GCM)
                    .put("AES256_GCM", NISTObjectIdentifiers.id_aes256_GCM)
                    .put("AES128_WRAP", NISTObjectIdentifiers.id_aes128_wrap)
                    .put("AES192_WRAP", NISTObjectIdentifiers.id_aes192_wrap)
                    .put("AES256_WRAP", NISTObjectIdentifiers.id_aes256_wrap)
                    .put("CHACHA20_POLY1305", PKCSObjectIdentifiers.id_alg_AEADChaCha20Poly1305) // with CMS should be used data size 64 bytes or more
                    .build();

    @Getter
    private final ASN1ObjectIdentifier algorithm;

    @Inject
    public ASNCmsEncryptionConfig(CmsEncryptionConfig cmsEncryptionConfig) {
        String algo = cmsEncryptionConfig.getAlgo();

        if (!MAPPINGS.containsKey(algo)) {
            throw new IllegalArgumentException("Unknown ASN1 mapping for algo: " + algo);
        }

        algorithm = MAPPINGS.get(algo);
    }
}
