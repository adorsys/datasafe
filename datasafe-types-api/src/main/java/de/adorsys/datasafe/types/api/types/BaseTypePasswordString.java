package de.adorsys.datasafe.types.api.types;

import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Wrapper for password sensitive data.
 */
@Slf4j
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"toBeCleared", "cleared"} )
@ToString
public class BaseTypePasswordString {
    private char[] value;
    private AtomicBoolean toBeCleared = new AtomicBoolean(true);
    private AtomicBoolean cleared = new AtomicBoolean(false);

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
        toBeCleared.set(true);
        cleared.set(false);
    }

    /**
     * caller of method makes sure, supplied char[] is deleted asap
     *
     * @param value will stay unchanged
     */
    public BaseTypePasswordString(Supplier<char[]> value) {
        this.value = value.get();
        toBeCleared.set(false);
        cleared.set(false);
    }


    /**
     * clears the char array
     */
    public void clear() {
        synchronized (value) {
            if (toBeCleared.get()) {
                cleared.set(true);
                log.debug("CLEAR PASSWORD {}", this.getClass().getSimpleName());
                Arrays.fill(value, '0');
            }
        }
    }

    @SneakyThrows
    public char[] getValue() {
        synchronized (value) {
            if (cleared.get()) {
                throw new BaseTypePasswordStringException("Password was cleared before and must not be reused");
            }
            return value;
        }
    }

    @Override
    public String toString() {
        return "BaseTypePasswordString{" + Obfuscate.secureSensitiveChar(getValue()) + "}";
    }
}
