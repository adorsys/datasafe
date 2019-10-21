package de.adorsys.datasafe.encrypiton.api.types.keystore.pbkdf;

import lombok.Builder;
import lombok.Getter;

/**
 * PBKDF2-based key derivation.
 */
@Getter
@Builder
public class PBKDF2 {

    /**
     * Password derivation algorithm, for BCFKS refer to {@see org.bouncycastle.crypto.util.PBKDF2Config}
     */
    @Builder.Default
    private final String algo = "PRF_SHA512";

    /**
     * Password derivation salt length, for BCFKS refer to {@see org.bouncycastle.crypto.util.PBKDF2Config}
     */
    @Builder.Default
    private final int saltLength = 32;

    /**
     * Password derivation iteration count, for BCFKS refer to {@see org.bouncycastle.crypto.util.PBKDF2Config}
     */
    @Builder.Default
    private final int iterCount = 20480;
}
