package de.adorsys.docusafe2.business.api.keystore.types;

public class KeySourceAndKeyID {
    private final KeySource keySource;
    private final KeyID keyID;

    public KeySourceAndKeyID(KeySource keySource, KeyID keyID) {
        this.keySource = keySource;
        this.keyID = keyID;
    }

    public KeySource getKeySource() {
        return keySource;
    }

    public KeyID getKeyID() {
        return keyID;
    }
}
