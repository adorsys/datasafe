package de.adorsys.datasafe.business.impl.keystore.generator;

import de.adorsys.docusafe2.business.api.keystore.types.SecretKeyEntry;
import lombok.Builder;
import lombok.Getter;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

@Getter
public class SecretKeyData extends KeyEntryData implements SecretKeyEntry {

	private final SecretKey secretKey;
	private final String keyAlgo;

	@Builder
	private SecretKeyData(CallbackHandler passwordSource, String alias, SecretKey secretKey, String keyAlgo) {
		super(passwordSource, alias);
		this.secretKey = secretKey;
		this.keyAlgo = keyAlgo;
	}
}
