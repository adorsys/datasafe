package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObfuscateTest {

    private static final String TEST_STRING = "/path/to/file";
    private static final Uri TEST_URI = new Uri("http://www.example.com/uniform/resource/identifier");
    private static final Uri TEST_URI_WITH_CREDS =
            new Uri("http://username:password@www.example.com/uniform/resource/identifier");
    private static final Uri TEST_URI_ENDS_SLASH = new Uri("http://www.example.com/uniform/resource/identifier/");

    @Test
    void disabledHidingLog() {
        Obfuscate.secureLogs = "OFF";
        assertThat(Obfuscate.secure(TEST_STRING)).isEqualTo(TEST_STRING);
    }

    @Test
    void hidingWithStars() {
        Obfuscate.secureLogs = "STARS";
        assertThat(Obfuscate.secure(TEST_STRING)).isEqualTo("/p****le");
    }

    @Test
    void hidingWithHash() {
        Obfuscate.secureLogs = ""; // hash
        String out = Obfuscate.secure(TEST_STRING);
        assertThat(out).isNotEqualTo(TEST_STRING);
        assertThat(out.length()).isEqualTo(44);
    }

    @Test
    void secureUriWithHash() {
        Obfuscate.secureLogs = ""; // hash
        assertThat(TEST_URI.toString())
                .isEqualTo("Uri{uri=HDHdoHyx4J6JvWYKjRFJNrRPcotzo7xSxppAnuHUTmc=//" +
                        "gPwPuSZtt7g_hYUPoOZUi21w7mjItbQS8d7qbr3vBAQ=/7zeTY90Mt75mY4z3-HJocsj59vK0GtIYGSx2PAYtMQo=/" +
                        "XelTGfF0Z-1txY5OCxbBGToTs11g3Ei88Gv2t77rvmw=/KFQ324xeFUGqSXCLSr1RZCoGW2bkusCQHnW0TOLQzr4=}");
    }

    @Test
    void secureUriWithHashAndAuthorityIgnoresAuthority() {
        Obfuscate.secureLogs = ""; // hash
        assertThat(TEST_URI_WITH_CREDS.toString())
                .isEqualTo("Uri{uri=HDHdoHyx4J6JvWYKjRFJNrRPcotzo7xSxppAnuHUTmc=//" +
                        "gPwPuSZtt7g_hYUPoOZUi21w7mjItbQS8d7qbr3vBAQ=/7zeTY90Mt75mY4z3-HJocsj59vK0GtIYGSx2PAYtMQo=/" +
                        "XelTGfF0Z-1txY5OCxbBGToTs11g3Ei88Gv2t77rvmw=/KFQ324xeFUGqSXCLSr1RZCoGW2bkusCQHnW0TOLQzr4=}");
    }

    @Test
    void securePath() {
        Obfuscate.secureLogs = "STARS";
        assertThat(new Uri("file:///path/to/file").toString())
                .isEqualTo("Uri{uri=fi****e:///****/****/****}");
    }

    @Test
    void secureURI() {
        Obfuscate.secureLogs = "STARS";
        assertThat(TEST_URI.toString()).isEqualTo("Uri{uri=ht****p://ww****om/un****rm/re****ce/id****er}");
    }

    @Test
    void secureEmptyOrNull() {
        assertThat(Obfuscate.secure((String) null)).isNull();
        assertThat(Obfuscate.secureSensitive(null)).isNull();
        assertThat(Obfuscate.secure((String) null, "/")).isNull();
        assertThat(Obfuscate.secure("")).isNotEmpty();
    }

    @Test
    void secureSlashes() {
        assertThat(Obfuscate.secure("/", "/")).isEqualTo("/");
        assertThat(Obfuscate.secure("//", "/")).isEqualTo("//");
        assertThat(Obfuscate.secure("///", "/")).isEqualTo("///");
    }

    @Test
    void secureURISlash() {
        Obfuscate.secureLogs = "STARS";
        assertThat(TEST_URI_ENDS_SLASH.toString())
                .isEqualTo("Uri{uri=ht****p://ww****om/un****rm/re****ce/id****er/}");
    }

    @Test
    void disabledHidingSecretRequiresSecureLogsOff() {
        Obfuscate.secureSensitive = "TRUE";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("****");

        Obfuscate.secureSensitive = "OFF";
        Obfuscate.secureLogs = "OFF";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo(TEST_STRING);
    }

    @Test
    void hidingSecretWithHash() {
        Obfuscate.secureSensitive = "HASH";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("hash:9eRM");
    }

    @Test
    void hidingSecretTrue() {
        Obfuscate.secureSensitive = "TRUE";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("****");
        Obfuscate.secureSensitive = "true";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("****");
    }

    @Test
    void hidingSecretNull() {
        Obfuscate.secureSensitive = null;
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("****");
    }

    @Test
    void hidingSecretEmpty() {
        Obfuscate.secureSensitive = "";
        assertThat(Obfuscate.secureSensitive(TEST_STRING)).isEqualTo("****");
    }

    @Test
    void obfuscateCharOn() {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(TEST_STRING);
        Obfuscate.secureSensitive = "";
        Assertions.assertTrue(readKeyPassword.toString().contains("****"));
        Assertions.assertFalse(readKeyPassword.toString().contains(TEST_STRING));
    }

    @Test
    void obfuscateCharOFF() {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(TEST_STRING);
        Obfuscate.secureSensitive = "OFF";
        Obfuscate.secureLogs = "OFF";
        Assertions.assertFalse(readKeyPassword.toString().contains("****"));
        Assertions.assertTrue(readKeyPassword.toString().contains(TEST_STRING));
    }
}
