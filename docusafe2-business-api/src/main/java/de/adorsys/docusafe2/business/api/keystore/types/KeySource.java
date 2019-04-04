package de.adorsys.docusafe2.business.api.keystore.types;

import java.security.Key;

/**
 * Retrieves and returns the key with the corresponding keyId.
 *
 * @author fpo
 */
public interface KeySource {
    Key readKey(KeyID keyID);
}
