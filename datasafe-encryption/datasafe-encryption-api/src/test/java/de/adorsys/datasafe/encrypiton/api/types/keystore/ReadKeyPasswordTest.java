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
        assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isFalse();

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
        assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();

    }


    @Test
    public void testWithDeprecatedConstructor() {
        String passwordString = "that is the password";
        char[] copyOfPassword = Arrays.copyOf(passwordString.toCharArray(), passwordString.toCharArray().length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(passwordString);
        ReadKeyPassword readKeyPasswordBackup = new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return readKeyPassword.getValue();
            }
        });
        assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        readKeyPassword.clear();
        assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();
    }

    @Test
    public void overwriteString() {
        String s = "peter";
        s.toCharArray()[0] = 'P';
        assertThat(s.equals("Peter")).isFalse();
    }
}
