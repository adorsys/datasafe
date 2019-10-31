package de.adorsys.datasafe.encrypiton.api.types.encryption;

import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import lombok.Builder;
import lombok.Getter;

/**
 * Full encryption configuration.
 */
@Getter
@Builder(toBuilder = true)
public class EncryptionConfig {

    /**
     * Which keystore to use and how to encrypt keys in it.
     */
    @Builder.Default
    private final KeyStoreConfig keystore = KeyStoreConfig.builder().build();

    /**
     * Which keys to create in keystore.
     */
    @Builder.Default
    private final KeyCreationConfig keys = KeyCreationConfig.builder().build();

    /**
     * How to encrypt documents.
     */
    @Builder.Default
    private final CmsEncryptionConfig cms = CmsEncryptionConfig.builder().build();
}
