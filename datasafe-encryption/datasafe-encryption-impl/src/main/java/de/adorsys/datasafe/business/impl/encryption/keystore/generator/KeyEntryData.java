package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.KeyEntry;
import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.security.auth.callback.CallbackHandler;

@Getter
@AllArgsConstructor
abstract class KeyEntryData implements KeyEntry {

	private final ReadKeyPassword readKeyPassword;
	
	private final String alias;
}
