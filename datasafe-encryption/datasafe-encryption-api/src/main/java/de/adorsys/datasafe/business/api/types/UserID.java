package de.adorsys.datasafe.business.api.types;

import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

@EqualsAndHashCode(of = "value")
public class UserID {

    @Delegate
    private final BaseTypeString value;

    public UserID(String value) {
        this.value = new BaseTypeString(value);
    }

    @Override
    public String toString() {
        return value.getValue();
    }
}