package de.adorsys.datasafe.business.api.types.utils;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class LogTest {

    private static final String TEST_STRING = "/path/to/file";
    private static final URI TEST_URI = URI.create("http://www.example.com/uniform/resource/identifier");
    private static final URI TEST_URI_ENDS_SLASH = URI.create("http://www.example.com/uniform/resource/identifier/");

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
    void secureResourceLocation() {
        Log.secureLogs = "STARS";
        AbsoluteResourceLocation<PrivateResource> resource =
                new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(TEST_URI));
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
        AbsoluteResourceLocation<PrivateResource> resource =
                new AbsoluteResourceLocation<>(DefaultPrivateResource.forPrivate(TEST_URI_ENDS_SLASH));
        assertThat(Log.secure(resource)).isEqualTo("ht****p://ww****om/un****rm/re****ce/id****er/");
    }
}