package de.adorsys.docusafe2.keystore.api.types;

import com.nimbusds.jose.jwk.JWK;

/**
 * Created by peter on 19.11.18 15:21.
 */
public class PublicKeyJWK {
    private JWK value;
    public PublicKeyJWK(JWK jwk) {
        value = jwk;
    }

    public JWK getValue() {
        return value;
    }
}
