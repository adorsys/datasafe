package de.adorsys.datasafe.encrypiton.api.types;

import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

/**
 * Wrapper that represents username.
 */
@EqualsAndHashCode(of = "value")
public class UserID {

    @Delegate
    private final BaseTypeString value;

    public UserID(String value) {
        this.value = new BaseTypeString(value);
    }

    @Override
    public String toString() {
        return Obfuscate.secure(value.getValue());
    }
}
