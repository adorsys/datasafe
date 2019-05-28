package de.adorsys.datasafe.encrypiton.impl.keystore.types;


import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyEntry;

public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();
}
