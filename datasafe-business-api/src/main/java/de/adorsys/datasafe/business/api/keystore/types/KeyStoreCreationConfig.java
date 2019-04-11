package de.adorsys.datasafe.business.api.keystore.types;

/**
 * Created by peter on 26.02.18 at 17:04.
 */
public class KeyStoreCreationConfig {
    private final Integer encKeyNumber;
    private final Integer signKeyNumber;
    private final Integer secretKeyNumber;

    public KeyStoreCreationConfig(Integer encKeyNumber, Integer signKeyNumber, Integer secretKeyNumber) {
        this.encKeyNumber = encKeyNumber;
        this.signKeyNumber = signKeyNumber;
        this.secretKeyNumber = secretKeyNumber;
    }

    public Integer getEncKeyNumber() {
        return encKeyNumber;
    }

    public Integer getSignKeyNumber() {
        return signKeyNumber;
    }

    public Integer getSecretKeyNumber() {
        return secretKeyNumber;
    }
}
