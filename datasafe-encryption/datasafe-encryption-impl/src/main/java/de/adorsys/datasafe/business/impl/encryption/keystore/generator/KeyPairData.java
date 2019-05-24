package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.impl.encryption.keystore.types.KeyPairEntry;
import de.adorsys.datasafe.business.impl.encryption.keystore.types.SelfSignedKeyPairData;
import lombok.Builder;
import lombok.Getter;

@Getter
public class KeyPairData extends KeyEntryData implements KeyPairEntry {

    private final SelfSignedKeyPairData keyPair;

    @Builder
    private KeyPairData(ReadKeyPassword readKeyPassword, String alias, SelfSignedKeyPairData keyPair) {
        super(readKeyPassword, alias);
        this.keyPair = keyPair;
    }
}
