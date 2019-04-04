package de.adorsys.docusafe2.keystore.impl;

import de.adorsys.docusafe2.keystore.api.types.KeyEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.security.auth.callback.CallbackHandler;

@Getter
@AllArgsConstructor
abstract class KeyEntryData implements KeyEntry {

	private final CallbackHandler passwordSource;
	
	private final String alias;
}
