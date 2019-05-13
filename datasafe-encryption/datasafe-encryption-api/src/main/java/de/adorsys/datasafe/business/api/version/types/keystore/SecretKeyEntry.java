package de.adorsys.datasafe.business.api.version.types.keystore;

import javax.crypto.SecretKey;

public interface SecretKeyEntry extends KeyEntry {
    SecretKey getSecretKey();

    String getKeyAlgo();
}
