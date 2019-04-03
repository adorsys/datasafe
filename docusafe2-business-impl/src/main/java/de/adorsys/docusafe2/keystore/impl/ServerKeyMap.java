package de.adorsys.docusafe2.keystore.impl;

import com.nimbusds.jose.jwk.*;
import org.apache.commons.lang3.RandomUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerKeyMap {
    private Map<String, KeyAndJwk> keyMap = new HashMap<>();
    private List<KeyAndJwk> signKeyList = new ArrayList<>();
    private List<KeyAndJwk> encKeyList = new ArrayList<>();
    private List<KeyAndJwk> secretKeyList = new ArrayList<>();
    
    public ServerKeyMap(JWKSet jwkSet){
        List<JWK> keys = jwkSet.getKeys();
        for (JWK jwk : keys) {
            if (jwk instanceof AssymetricJWK) {
            	Key key = KeyConverter.toPrivateOrSecret(jwk);
            	if(key!=null && jwk.getKeyID()!=null){
            		KeyAndJwk keyAndJwk = new KeyAndJwk(key, jwk);
            		keyMap.put(jwk.getKeyID(), keyAndJwk);
            		if(KeyUse.SIGNATURE.equals(jwk.getKeyUse())){
            			signKeyList.add(keyAndJwk);
            		} else if (KeyUse.ENCRYPTION.equals(jwk.getKeyUse())){
            			encKeyList.add(keyAndJwk);
            		}
            	}
            } else if (jwk instanceof SecretJWK) {
            	Key key = KeyConverter.toPrivateOrSecret(jwk);
            	if(key!=null && jwk.getKeyID()!=null){
            		KeyAndJwk keyAndJwk = new KeyAndJwk(key, jwk);
            		keyMap.put(jwk.getKeyID(), keyAndJwk);
            		secretKeyList.add(keyAndJwk);
            	}
            }

        }
    }

    private KeyAndJwk get(String keyID){
        if(keyID==null) return null;
        KeyAndJwk keyAndJwk = keyMap.get(keyID);
        if(keyAndJwk==null) return null;
        if(!keyID.equalsIgnoreCase(keyAndJwk.jwk.getKeyID()))return null;
        return keyAndJwk;
    }

    public Key getKey(String keyID){
        KeyAndJwk keyAndJwk = get(keyID);
        if(keyAndJwk==null) return null;
        return keyAndJwk.key;
    }

	
	/**
	 * Select a random key by random picking a number between 0 (inclusive) and size exclusive;
     * @return KeyAndJwk keyAndJwk
	 */
    public KeyAndJwk randomSignKey(){
    	int nextInt = RandomUtils.nextInt(0, signKeyList.size());
    	return signKeyList.get(nextInt);
    }

	public KeyAndJwk randomSecretKey() {
    	int nextInt = RandomUtils.nextInt(0, secretKeyList.size());
    	return secretKeyList.get(nextInt);
	}
    
}
