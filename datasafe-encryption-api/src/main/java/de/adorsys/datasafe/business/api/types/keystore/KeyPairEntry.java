package de.adorsys.datasafe.business.api.types.keystore;


public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
