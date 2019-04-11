package de.adorsys.datasafe.business.api.keystore.types;


public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
