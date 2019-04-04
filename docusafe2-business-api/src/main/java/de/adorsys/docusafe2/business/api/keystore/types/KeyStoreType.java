package de.adorsys.docusafe2.business.api.keystore.types;

import de.adorsys.common.basetypes.BaseTypeString;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by peter on 29.12.2017 at 14:11:52.
 */
public class KeyStoreType extends BaseTypeString {
    public static KeyStoreType DEFAULT = getDefaultKeyStoreType();

	public KeyStoreType() {}

    public KeyStoreType(String value) {
        super(value);
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTypeString that = (BaseTypeString) o;

        return StringUtils.equalsAnyIgnoreCase(getValue(), that.getValue());
    }

    private static KeyStoreType getDefaultKeyStoreType() {
        String server_keystore_type = System.getProperty("SERVER_KEYSTORE_TYPE");
        if (!StringUtils.isBlank(server_keystore_type )) {
            return new KeyStoreType(server_keystore_type);
        }
        return new KeyStoreType("UBER");
    }
    
    
    
}
