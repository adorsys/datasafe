package de.adorsys.datasafe.encrypiton.impl;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.security.KeyStore;
import java.util.Enumeration;

@UtilityClass
public class KeystoreUtil {

    @SneakyThrows
    public KeyID keyIdByPrefix(KeyStore keyStore, String prefix) {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String element = aliases.nextElement();
            if (element.startsWith(prefix)) {
                return new KeyID(element);
            }
        }

        throw new IllegalArgumentException("Keystore does not contain key with prefix: " + prefix);
    }
}
