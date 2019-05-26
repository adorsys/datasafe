package de.adorsys.datasafe.encrypiton.api.types.keystore;

import javax.crypto.SecretKey;

public interface SecretKeyEntry extends KeyEntry {
    SecretKey getSecretKey();

    String getKeyAlgo();
}
