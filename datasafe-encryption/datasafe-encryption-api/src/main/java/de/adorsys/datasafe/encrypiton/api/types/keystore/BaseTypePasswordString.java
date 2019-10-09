package de.adorsys.datasafe.encrypiton.api.types.keystore;

import de.adorsys.datasafe.encrypiton.api.types.BaseTypeString;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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
     *
     * @param value will be invalidated after usage
     */
    public BaseTypePasswordString(char[] value) {
        this.value = value;
    }


    public void clear() {
        log.info("CLEAR READ KEY PASSWORD");
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{-not-supported-yet}";
        // return "BaseTypePasswordString{" + Obfuscate.secureSensitive(getValue()) + "}";
    }
}
