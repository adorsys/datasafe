package de.adorsys.docusafe2.business.impl.keystore.generator;

import de.adorsys.docusafe2.business.api.keystore.types.KeyEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.security.auth.callback.CallbackHandler;

@Getter
@AllArgsConstructor
abstract class KeyEntryData implements KeyEntry {

	private final CallbackHandler passwordSource;
	
	private final String alias;
}
