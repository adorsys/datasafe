package de.adorsys.docusafe2.keystore.api.types;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyID extends BaseTypeString {

	public KeyID() {}

    public KeyID(String value) {
        super(value);
    }
}
