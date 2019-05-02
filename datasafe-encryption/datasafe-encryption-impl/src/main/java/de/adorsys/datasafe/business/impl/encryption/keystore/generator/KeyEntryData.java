package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.KeyEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.security.auth.callback.CallbackHandler;

@Getter
@AllArgsConstructor
abstract class KeyEntryData implements KeyEntry {

	private final CallbackHandler passwordSource;
	
	private final String alias;
}
