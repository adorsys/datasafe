package de.adorsys.docusafe2.business.api.keystore.types;


public interface KeyPairEntry extends KeyEntry {
    SelfSignedKeyPairData getKeyPair();

    CertificationResult getCertification();
}
