package de.adorsys.datasafe.encrypiton.api.types.encryption;

import lombok.Builder;
import lombok.Getter;

/**
 * Configures document-body encryption algorithm.
 * {@see de.adorsys.datasafe.encrypiton.impl.cmsencryption.ASNCmsEncryptionConfig} for values or override it.
 */
@Getter
@Builder(toBuilder = true)
public class CmsEncryptionConfig {

    @Builder.Default
    private final String algo = "AES256_GCM";
}
