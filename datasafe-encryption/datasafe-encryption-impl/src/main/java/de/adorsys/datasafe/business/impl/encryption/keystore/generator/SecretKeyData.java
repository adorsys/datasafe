package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.keystore.SecretKeyEntry;
import lombok.Builder;
import lombok.Getter;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

@Getter
public class SecretKeyData extends KeyEntryData implements SecretKeyEntry {

	private final SecretKey secretKey;
	private final String keyAlgo;

	@Builder
	private SecretKeyData(ReadKeyPassword readKeyPassword, String alias, SecretKey secretKey, String keyAlgo) {
		super(readKeyPassword, alias);
		this.secretKey = secretKey;
		this.keyAlgo = keyAlgo;
	}
}
