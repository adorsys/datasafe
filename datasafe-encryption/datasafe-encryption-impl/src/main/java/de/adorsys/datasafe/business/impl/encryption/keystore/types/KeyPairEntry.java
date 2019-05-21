package de.adorsys.datasafe.business.impl.encryption.keystore.types;


import de.adorsys.datasafe.business.api.types.keystore.KeyEntry;

public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();
}
