package de.adorsys.docusafe2.business.impl.keystore;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.docusafe2.business.api.keystore.types.KeyID;
import de.adorsys.docusafe2.business.api.keystore.types.KeySource;
import de.adorsys.docusafe2.business.api.keystore.types.ReadKeyPassword;

import java.security.Key;
import java.security.KeyStore;

/**
 * Created by peter on 26.02.18 at 14:00.
 */
public class KeyStoreBasedPrivateKeySourceImpl implements KeySource {

    private KeyStore keyStore;
    private ReadKeyPassword readKeyPassword;


    public KeyStoreBasedPrivateKeySourceImpl(KeyStore keyStore, ReadKeyPassword readKeyPassword) {
        this.keyStore = keyStore;
        this.readKeyPassword = readKeyPassword;
    }


    @Override
    public Key readKey(KeyID keyID) {
        try {
            return keyStore.getKey(keyID.getValue(), readKeyPassword.getValue().toCharArray());
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }
    }
}
