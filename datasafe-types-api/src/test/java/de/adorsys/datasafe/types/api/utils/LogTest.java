package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class LogTest {

    private static final String TEST_STRING = "/path/to/file";
    private static final Uri TEST_URI = new Uri("http://www.example.com/uniform/resource/identifier");
    private static final Uri TEST_URI_ENDS_SLASH = new Uri("http://www.example.com/uniform/resource/identifier/");

    @Test
    void disabledHidingLog() {
        Log.secureLogs = "OFF";
        assertThat(Log.secure(TEST_STRING)).isEqualTo(TEST_STRING);
    }

    @Test
    void hidingWithStars() {
        Log.secureLogs = "STARS";
        assertThat(Log.secure(TEST_STRING)).isEqualTo("/p****le");
    }

    @Test
    void hidingWithHash() {
        Log.secureLogs = ""; // hash
        String out = Log.secure(TEST_STRING);
        assertThat(out).isNotEqualTo(TEST_STRING);
        assertThat(out.length()).isEqualTo(44);
    }

    @Test
    void securePath() {
        Log.secureLogs = "STARS";
        assertThat(Log.secure(Paths.get(TEST_STRING))).isEqualTo("fi****e:///****/****/****");
    }

    @Test
    void secureURI() {
        Log.secureLogs = "STARS";
        assertThat(Log.secure(TEST_URI)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er");
    }

    @Test
    void secureNullPath() {
        Path path = null;
        assertThat(Log.secure(path)).isNull();
    }

    @Test
    void secureNullUri() {
        URI uri = null;
        assertThat(Log.secure(uri)).isNull();
    }

    @Test
    void secureNullObject() {
        Object uri = null;
        assertThat(Log.secure(uri)).isNull();
    }

    @Test
    void secureResourceLocation() {
        Log.secureLogs = "STARS";
        AbsoluteLocation<PrivateResource> resource =
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(TEST_URI));
        assertThat(Log.secure(resource)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er");
    }

    @Test
    void secureURISlash() {
        Log.secureLogs = "STARS";
        assertThat(Log.secure(TEST_URI_ENDS_SLASH)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er/");
    }

    @Test
    void secureResourceLocationSlash() {
        Log.secureLogs = "STARS";
        AbsoluteLocation<PrivateResource> resource =
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(TEST_URI_ENDS_SLASH));
        assertThat(Log.secure(resource)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er/");
    }

    @Test
    void disabledHidingSecretRequiresSecureLogsOff() {
        Log.secureSensitive = "OFF";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("****");

        Log.secureSensitive = "OFF";
        Log.secureLogs = "OFF";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo(TEST_STRING);
    }

    @Test
    void hidingSecretWithHash() {
        Log.secureSensitive = "HASH";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("hash:9eRM");
    }

    @Test
    void hidingSecretTrue() {
        Log.secureSensitive = "TRUE";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("****");
        Log.secureSensitive = "true";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("****");
    }

    @Test
    void hidingSecretNull() {
        Log.secureSensitive = null;
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("****");
    }

    @Test
    void hidingSecretEmpty() {
        Log.secureSensitive = "";
        assertThat(Log.secureSensitive(TEST_STRING)).isEqualTo("****");
    }
}
