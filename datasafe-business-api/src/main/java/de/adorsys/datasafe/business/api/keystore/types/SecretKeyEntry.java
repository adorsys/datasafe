package de.adorsys.datasafe.business.api.keystore.types;

import javax.crypto.SecretKey;

public interface SecretKeyEntry extends KeyEntry {
    SecretKey getSecretKey();

    String getKeyAlgo();
}
