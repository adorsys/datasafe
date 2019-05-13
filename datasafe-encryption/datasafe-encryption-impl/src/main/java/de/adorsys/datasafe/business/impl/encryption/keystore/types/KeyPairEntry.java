package de.adorsys.datasafe.business.impl.encryption.keystore.types;


import de.adorsys.datasafe.business.api.version.types.keystore.KeyEntry;

public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
