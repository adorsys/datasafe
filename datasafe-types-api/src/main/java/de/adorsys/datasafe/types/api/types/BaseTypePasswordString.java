package de.adorsys.datasafe.types.api.types;

import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Wrapper for password sensitive data.
 */
@Slf4j
@EqualsAndHashCode
public class BaseTypePasswordString {

    private final char[] value;
    private Supplier<char[]> passwordSupplier;
    private boolean cleared = false;

    /**
     * ATTENTION
     * <p>
     * caller of method gives ownership of {@code value[]}
     * to this class. Value will be nullyfied after successful read/write/list.
     *
     * @param value will be nullified asap
     */
    public BaseTypePasswordString(char[] value) {
        this.value = value;
        passwordSupplier = null;
    }

    /**
     * Argument provider is responsible for password cleanup
     * @param value will stay unchanged
     */
    public BaseTypePasswordString(Supplier<char[]> value) {
        this.passwordSupplier = value;
        this.value = null;
    }

    /**
     * clears the char array
     */
    @Synchronized
    public void clear() {
        if (null != value) {
            Arrays.fill(value, '0');
        }
        cleared = true;
    }

    /**
     * Note that returned value is not immutable.
     */
    @Synchronized
    @SneakyThrows
    public char[] getValue() {
        if (null != passwordSupplier) {
            return passwordSupplier.get();
        }

        if (cleared) {
            throw new BaseTypePasswordStringException("Password was cleared before and must not be reused");
        }

        return value;
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{" + Obfuscate.secureSensitiveChar(getValue()) + "}";
    }
}
