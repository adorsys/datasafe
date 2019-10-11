package de.adorsys.datasafe.types.api.types;

import lombok.*;
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
    private boolean toBeCleared = true;

    /**
     *
     * @param value string stays in memory
     *             until gc is called.
     *             please user other constructor.
     */
    protected BaseTypePasswordString(String value) {
        this.value = value.toCharArray();
        toBeCleared = false;
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
        toBeCleared = true;
    }

    /**
     * caller of method makes sure, supplied char[] is deleted asap
     *
     * @param value will stay unchanged
     */
    public BaseTypePasswordString(Supplier<char[]> value) {
        this.value = value.get();
        toBeCleared = false;
    }


    /**
     * Hi Valentyn, dont understand synchronized in this context.
     * Java synchronized is too expensive, as there may run several threads
     * all using a password. synchronizing with user not possible as
     * user is not known here. So how should I synchronize ?
     */
    public void clear() {
        if (toBeCleared) {
            log.warn("CLEAR PASSWORD {}", this.getClass().getSimpleName());
            Arrays.fill(value, '0');
        }
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{-not-supported-yet}";
        // return "BaseTypePasswordString{" + Obfuscate.secureSensitive(getValue()) + "}";
    }
}
