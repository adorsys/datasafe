package de.adorsys.datasafe.encrypiton.api.types.keystore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Wrapper for password sensitive data.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class BaseTypePasswordString {
    private char[] value;

    @Deprecated
    /**
     *
     * @param this string stays in memory
     *             until gc is called.
     *             please user other constructor.
     */
    public BaseTypePasswordString(String value) {
        this.value = value.toCharArray();
    }

    /**
     * ATTENTION
     * <p>
     * caller of method gives responsiblity of char[]
     * to this class. char[] will be nullyfied
     * asap (after successfull read/write/list)
     *
     * @param value will be nullified asap
     */
    public BaseTypePasswordString(char[] value) {
        this.value = value;
    }

    /**
     * caller of method makes sure, supplied char[] is deleted asap
     *
     * @param value will stay unchanged
     */
    public BaseTypePasswordString(Supplier<char[]> value) {
        this.value = Arrays.copyOf(value.get(), value.get().length);
    }


    public void clear() {
        log.warn("CLEAR PASSWORD {}", this.getClass().getSimpleName());
        for (int i = 0; i < value.length; i++) {
            value[i] = '0';
        }
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{-not-supported-yet}";
        // return "BaseTypePasswordString{" + Obfuscate.secureSensitive(getValue()) + "}";
    }
}
