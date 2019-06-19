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

class ObfuscateTest {

    private static final String TEST_STRING = "/path/to/file";
    private static final Uri TEST_URI = new Uri("http://www.example.com/uniform/resource/identifier");
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
    void securePath() {
        Obfuscate.secureLogs = "STARS";
        assertThat(Obfuscate.secure(Paths.get(TEST_STRING))).isEqualTo("fi****e:///****/****/****");
    }

    @Test
    void secureURI() {
        Obfuscate.secureLogs = "STARS";
        assertThat(Obfuscate.secure(TEST_URI)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er");
    }

    @Test
    void secureNullPath() {
        Path path = null;
        assertThat(Obfuscate.secure(path)).isNull();
    }

    @Test
    void secureNullUri() {
        URI uri = null;
        assertThat(Obfuscate.secure(uri)).isNull();
    }

    @Test
    void secureEmptyOrNull() {
        assertThat(Obfuscate.secure((String) null)).isNull();
        assertThat(Obfuscate.secure((Uri) null)).isNull();
        assertThat(Obfuscate.secure((URI) null)).isNull();
        assertThat(Obfuscate.secure((Object) null)).isNull();
        assertThat(Obfuscate.secureSensitive(null)).isNull();
        assertThat(Obfuscate.secure((Object[]) null)).isNull();
        assertThat(Obfuscate.secure((String) null, "/")).isNull();
        assertThat(Obfuscate.secure((Iterable) null, "/")).isNull();
        assertThat(Obfuscate.secure("")).isNotEmpty();
    }

    @Test
    void secureSlashes() {
        assertThat(Obfuscate.secure("/", "/")).isEmpty();
        assertThat(Obfuscate.secure("//", "/")).isEmpty();
        assertThat(Obfuscate.secure("///", "/")).isEmpty();
    }

    @Test
    void secureNullObject() {
        Object uri = null;
        assertThat(Obfuscate.secure(uri)).isNull();
    }

    @Test
    void secureResourceLocation() {
        Obfuscate.secureLogs = "STARS";
        AbsoluteLocation<PrivateResource> resource =
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(TEST_URI));
        assertThat(Obfuscate.secure(resource)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er");
    }

    @Test
    void secureURISlash() {
        Obfuscate.secureLogs = "STARS";
        assertThat(Obfuscate.secure(TEST_URI_ENDS_SLASH)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er/");
    }

    @Test
    void secureResourceLocationSlash() {
        Obfuscate.secureLogs = "STARS";
        AbsoluteLocation<PrivateResource> resource =
                new AbsoluteLocation<>(BasePrivateResource.forPrivate(TEST_URI_ENDS_SLASH));
        assertThat(Obfuscate.secure(resource)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er/");
    }

    @Test
    void disabledHidingSecretRequiresSecureLogsOff() {
        Obfuscate.secureSensitive = "OFF";
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
}
