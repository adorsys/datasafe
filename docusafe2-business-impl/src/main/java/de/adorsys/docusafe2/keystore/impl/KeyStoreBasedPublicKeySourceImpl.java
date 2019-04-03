package de.adorsys.docusafe2.keystore.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.AssymetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import de.adorsys.docusafe2.keystore.api.exceptions.KeySourceException;
import de.adorsys.docusafe2.keystore.api.types.KeyID;
import de.adorsys.docusafe2.keystore.api.types.KeySource;
import org.adorsys.cryptoutils.exceptions.BaseExceptionHandler;

import java.security.Key;

public class KeyStoreBasedPublicKeySourceImpl implements KeySource {


	private JWKSet keys;


	public KeyStoreBasedPublicKeySourceImpl(JWKSet keys) {
		this.keys = keys;
	}


	@Override
	public Key readKey(KeyID keyID) {
		JWK jwk = JwkExport.selectKey(keys, keyID.getValue());
		if (jwk instanceof AssymetricJWK) {
			try {
				return ((AssymetricJWK) jwk).toPublicKey();
			} catch (JOSEException e) {
				throw BaseExceptionHandler.handle(e);
			}
		} else {
			throw new KeySourceException("key with id " +keyID.getValue()  + " not instance of AssymetricJWK");
		}
	}
}
