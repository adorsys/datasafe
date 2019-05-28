package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Instantiates and returns a key pair certificate.
 * 
 * @author fpo
 *
 */
public class KeyPairBuilder {
	
	private Integer keyLength;
	private String keyAlg;
	
	boolean dirty = false;

	/**
	 * Returns the message key pair subject certificate holder.
	 * @return KeyPair
	 */
	public KeyPair build() {
		if(dirty)throw new IllegalStateException("Builder can not be reused");
		dirty=true;
		List<KeyValue> notNullCheckList = ListOfKeyValueBuilder.newBuilder()
				.add("keyAlg", keyAlg)
				.add("keyLength", keyLength)
				.build();
		List<String> nullList = BatchValidator.filterNull(notNullCheckList);
		if(nullList!=null && !nullList.isEmpty()){
			throw new IllegalArgumentException("Fields can not be null: " + nullList);
		}
		// Generate a key pair for the new EndEntity
		KeyPairGenerator kGen;
		try {
			kGen = KeyPairGenerator.getInstance(keyAlg, ProviderUtils.bcProvider);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}

		kGen.initialize(keyLength);
		return kGen.generateKeyPair();
	}

	public KeyPairBuilder withKeyLength(Integer keyLength) {
		this.keyLength = keyLength;
		return this;
	}

	public KeyPairBuilder withKeyAlg(String keyAlg) {
		this.keyAlg = keyAlg;
		return this;
	}
}
