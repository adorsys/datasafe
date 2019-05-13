package de.adorsys.datasafe.business.api.version.types;

import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

@EqualsAndHashCode(of = "value")
public class UserID {

    @Delegate
    private final BaseTypeString value;

    public UserID(String value) {
        this.value = new BaseTypeString(value);
    }
}