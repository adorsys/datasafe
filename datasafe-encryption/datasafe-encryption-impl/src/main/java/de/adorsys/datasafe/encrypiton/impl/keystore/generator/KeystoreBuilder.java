package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyEntry;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreCreationConfig;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class KeystoreBuilder {
	private KeyStoreCreationConfig keyStoreConfig;
	private Map<String, KeyEntry> keyEntries = new HashMap<>();
	
	public KeystoreBuilder withKeyStoreConfig(KeyStoreCreationConfig keyStoreConfig) {
		this.keyStoreConfig = keyStoreConfig;
		return this;
	}

	public KeystoreBuilder withKeyEntry(KeyEntry keyEntry) {
		this.keyEntries.put(keyEntry.getAlias(), keyEntry);
		return this;
	}

	public KeyStore build() {
		KeyStore ks = KeyStoreServiceImplBaseFunctions.newKeyStore(keyStoreConfig);
		KeyStoreServiceImplBaseFunctions.fillKeyStore(ks, keyEntries.values());

		return ks;
	}
}
