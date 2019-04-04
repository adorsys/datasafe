package de.adorsys.docusafe2.business.impl.keystore;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.docusafe2.business.api.keystore.types.KeyID;
import de.adorsys.docusafe2.business.api.keystore.types.KeySource;

import javax.security.auth.callback.CallbackHandler;
import java.security.*;

public class KeyStoreBasedSecretKeySourceImpl implements KeySource {

	private KeyStore keyStore;
	private CallbackHandler keyPassHandler;
	
	public KeyStoreBasedSecretKeySourceImpl(KeyStore keyStore, CallbackHandler keyPassHandler) {
		super();
		this.keyStore = keyStore;
		this.keyPassHandler = keyPassHandler;
	}


	@Override
	public Key readKey(KeyID keyID) {
		return readKey(keyStore, keyID, keyPassHandler);
	}


	/*
	 * Retrieves the key with the given keyID from the keystore. The key
	 * password will be retrieved by calling the keyPassHandler.
	 */
	private Key readKey(KeyStore keyStore, KeyID keyID, CallbackHandler keyPassHandler){
		try {
			return keyStore.getKey(keyID.getValue(), PasswordCallbackUtils.getPassword(keyPassHandler, keyID.getValue()));
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			throw BaseExceptionHandler.handle(e);
		}
	}
}
