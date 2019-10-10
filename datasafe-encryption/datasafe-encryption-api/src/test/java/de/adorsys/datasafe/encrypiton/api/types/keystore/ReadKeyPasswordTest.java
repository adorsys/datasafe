package de.adorsys.datasafe.encrypiton.api.types.keystore;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadKeyPasswordTest {
    String passwordString = "a password that should be nullyfied";
    @Test
    public void testClearanceForUnsupplied() {
        char[] password = passwordString.toCharArray();
        char[] copyOfPassword = Arrays.copyOf(password, password.length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(password);
        assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        assertThat(Arrays.equals(password, copyOfPassword)).isFalse();

    }

    @Test
    public void testClearanceForSupplied() {
        char[] password = passwordString.toCharArray();
        char[] copyOfPassword = Arrays.copyOf(password, password.length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return password;
            }
        });
        assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        assertThat(Arrays.equals(password, copyOfPassword)).isTrue();

    }
}
