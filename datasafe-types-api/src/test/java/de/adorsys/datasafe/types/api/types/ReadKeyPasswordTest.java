package de.adorsys.datasafe.types.api.types;

import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ReadKeyPasswordTest {
    
    private static final String passwordString = "a password that should be nullyfied";
    
    @Test
    void testClearanceForUnsupplied() {
        char[] password = passwordString.toCharArray();
        char[] copyOfPassword = Arrays.copyOf(password, password.length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(password);
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isFalse();
        assertThrows(BaseTypePasswordStringException.class, readKeyPassword::getValue);

    }

    @Test
    void testClearanceForSupplied() {
        char[] password = passwordString.toCharArray();
        char[] copyOfPassword = Arrays.copyOf(password, password.length);

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(() -> password);
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(password, copyOfPassword)).isTrue();
        Assertions.assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();
    }


    @Test
    void testWithDeprecatedConstructor() {
        String passwordString = "that is the password";
        char[] copyOfPassword = Arrays.copyOf(passwordString.toCharArray(), passwordString.toCharArray().length);

        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(passwordString);
        Assertions.assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        readKeyPassword.clear();
        Assertions.assertThat(Arrays.equals(passwordString.toCharArray(), copyOfPassword)).isTrue();
        Assertions.assertThat(Arrays.equals(readKeyPassword.getValue(), copyOfPassword)).isTrue();
    }

    @Test
    void overwriteString() {
        String s = "peter";
        s.toCharArray()[0] = 'P';
        Assertions.assertThat(s.equals("Peter")).isFalse();
    }

    @Test
    void useOnceOnly() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("peter".toCharArray());
        readKeyPassword.clear();
        assertThrows(BaseTypePasswordStringException.class, readKeyPassword::getValue);
    }

}
