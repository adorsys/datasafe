package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyEntry;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
abstract class KeyEntryData implements KeyEntry {

	private final ReadKeyPassword readKeyPassword;
	
	private final String alias;
}
