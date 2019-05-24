package de.adorsys.datasafe.business.impl.encryption.keystore.types;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;

/**
 * Created by peter on 26.02.18 at 17:09.
 */
public interface KeyPairGenerator {

    KeyPairEntry generateSignatureKey(String alias, ReadKeyPassword readKeyPassword);
    KeyPairEntry generateEncryptionKey(String alias, ReadKeyPassword readKeyPassword);
}
