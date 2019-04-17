package de.adorsys.datasafe.business.api.deployment.keystore.types;


public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
