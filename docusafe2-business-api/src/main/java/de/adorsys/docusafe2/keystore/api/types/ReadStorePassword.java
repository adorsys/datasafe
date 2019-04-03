package de.adorsys.docusafe2.keystore.api.types;

import org.adorsys.cryptoutils.basetypes.BaseTypePasswordString;

/**
 * Created by peter on 09.01.18 at 08:04.
 */
public class ReadStorePassword extends BaseTypePasswordString {
    public ReadStorePassword(String readStorePassword) {
        super(readStorePassword);
    }
}
