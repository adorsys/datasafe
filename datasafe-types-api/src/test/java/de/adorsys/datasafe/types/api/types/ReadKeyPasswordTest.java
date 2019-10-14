package de.adorsys.datasafe.types.api.types;

import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class ReadKeyPasswordTest {
    String passwordString = "a password that should be nullyfied";
    @Test
    public void testClearanceForUnsupplied() {
        char[] password = passwordString.toCharArray();
        char[] copyOfPassword = Arrays.copyOf(password, password.length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(password);
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isFalse();
        Assertions.assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isFalse();

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
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        Assertions.assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();

    }


    @Test
    public void testWithDeprecatedConstructor() {
        String passwordString = "that is the password";
        char[] copyOfPassword = Arrays.copyOf(passwordString.toCharArray(), passwordString.toCharArray().length);

        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(passwordString);
        ReadKeyPassword readKeyPasswordBackup = new ReadKeyPassword(new Supplier<char[]>() {
            @Override
            public char[] get() {
                return readKeyPassword.getValue();
            }
        });
        Assertions.assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        Assertions.assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();
    }

    @Test
    public void overwriteString() {
        String s = "peter";
        s.toCharArray()[0] = 'P';
        Assertions.assertThat(s.equals("Peter")).isFalse();
    }


    @Test
    public void useOnceOnly() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("peter".toCharArray());
        readKeyPassword.clear();
        assertThrows(BaseTypePasswordStringException.class, () -> readKeyPassword.getValue());
    }

}
