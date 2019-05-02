package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.List;

public class SecretKeyBuilder {

	private static Provider provider = ProviderUtils.bcProvider;
	
	private Integer keyLength;
	private String keyAlg;
	
	boolean dirty = false;
	/**
	 * Returns the message key pair subject certificate holder.
	 *
	 * Following entity must be validated
	 *
	 * @return KeyPairAndCertificateHolder
	 */
	public SecretKey build() {
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
		KeyGenerator kGen;
		try {
			kGen = KeyGenerator.getInstance(keyAlg, provider);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}

		kGen.init(keyLength);
		return kGen.generateKey();
	}

	public SecretKeyBuilder withKeyLength(Integer keyLength) {
		this.keyLength = keyLength;
		return this;
	}

	public SecretKeyBuilder withKeyAlg(String keyAlg) {
		this.keyAlg = keyAlg;
		return this;
	}

}
