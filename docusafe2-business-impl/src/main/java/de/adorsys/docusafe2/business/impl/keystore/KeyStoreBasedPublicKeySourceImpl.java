package de.adorsys.docusafe2.business.impl.keystore;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.AssymetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.docusafe2.business.api.keystore.exceptions.KeySourceException;
import de.adorsys.docusafe2.business.api.keystore.types.KeyID;
import de.adorsys.docusafe2.business.api.keystore.types.KeySource;

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
