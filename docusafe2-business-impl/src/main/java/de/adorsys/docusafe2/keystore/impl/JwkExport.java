package de.adorsys.docusafe2.keystore.impl;

import com.nimbusds.jose.jwk.*;
import org.apache.commons.lang3.RandomUtils;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility routines for reading keys from a key store into a JWKSet data structure.
 * 
 * @author fpo
 *
 */
public class JwkExport {
	public static List<JWK> selectSignKeys(JWKSet exportKeys){
//		JWKMatcher signKeys = new JWKMatcher.Builder().algorithms(JWSAlgorithm.Family.SIGNATURE.toArray(new JWSAlgorithm[]{})).build();
    	JWKMatcher signKeys = new JWKMatcher.Builder().keyUse(KeyUse.SIGNATURE).build();
    	return new JWKSelector(signKeys).select(exportKeys);
	}

	public static JWKSet exportPublicKeys(JWKSet exportKeys){
		return exportKeys.toPublicJWKSet();
	}
	
	public static List<JWK> selectKeypairs(JWKSet exportKeys){
		JWKSet publicJWKSet = exportKeys.toPublicJWKSet();
		List<JWK> keys = publicJWKSet.getKeys();
		if(keys==null || keys.isEmpty()) return keys;
		Set<String> keyIds = new HashSet<>();
		for (JWK jwk : keys) {
			keyIds.add(jwk.getKeyID());
		}
		JWKMatcher keyPairs = new JWKMatcher.Builder().keyIDs(keyIds).build();
    	return new JWKSelector(keyPairs).select(exportKeys);
	}
	
	public static JWK selectKey(JWKSet exportKeys, String keyId){
    	JWKMatcher matcher = new JWKMatcher.Builder().keyID(keyId).build();
    	List<JWK> keys = new JWKSelector(matcher).select(exportKeys);
    	if(keys==null || keys.isEmpty()) return null;
    	return keys.iterator().next();
	}
	

	public static JWKSet exportKeys(KeyStore keyStore, CallbackHandler callbackHandler){
		PasswordLookup pwLookup = new PasswordLookup() {
			@Override
			public char[] lookupPassword(String name) {
				return PasswordCallbackUtils.getPassword(callbackHandler, name);
			}
		};
		try {
			return JWKSet.load(keyStore, pwLookup);
		} catch (KeyStoreException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Select a random key by random picking a number between 0 (inclusive) and size exclusive;
	 * @param keys keys
	 * @return jwk
	 */
    public static JWK randomKey(List<JWK> keys){
    	int nextInt = RandomUtils.nextInt(0, keys.size());
    	return keys.get(nextInt);
    }
	
}
