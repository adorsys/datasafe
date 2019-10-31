package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class UriEncoderDecoderTest extends BaseMockitoTest {

    @Test
    void encodeRelative() {
        assertThat(UriEncoderDecoder.encode("уровень1/уровень 2=/&файл пробел+плюс"))
                .hasToString("%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1" +
                        "%8C%202%3D/%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0%BE%D0%B1%D0%B5%D0%BB%2B" +
                        "%D0%BF%D0%BB%D1%8E%D1%81");
    }

    @Test
    void encodeWithAbsolute() {
        assertThat(UriEncoderDecoder.encode("https://localhost:9090/уровень1/уровень 2=/?&файл пробел+плюс"))
                .hasToString("https://localhost:9090/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83%D1%80%D0%BE" +
                        "%D0%B2%D0%B5%D0%BD%D1%8C%202%3D/%3F%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0%BE%D0%B1%D0" +
                        "%B5%D0%BB%2B%D0%BF%D0%BB%D1%8E%D1%81");
    }

    @Test
    void encodeWithAuthority() {
        assertThat(
                UriEncoderDecoder.encode("https://user:password@localhost:9090/уровень1/уровень 2=/?&файл пробел+плюс")
        ).hasToString("https://user:password@localhost:9090/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83%D1%80" +
                "%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C%202%3D/%3F%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0%BE%D0%B1%D0" +
                "%B5%D0%BB%2B%D0%BF%D0%BB%D1%8E%D1%81");
    }

    @Test
    void decodeAndDropAuthorityRelative() {
        assertThat(UriEncoderDecoder.decodeAndDropAuthority(
                URI.create("%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1" +
                "%8C%202%3D/%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0%BE%D0%B1%D0%B5%D0%BB%2B" +
                "%D0%BF%D0%BB%D1%8E%D1%81"))).isEqualTo("уровень1/уровень 2=/&файл пробел+плюс");
    }

    @Test
    void decodeAndDropAuthorityAbsolute() {
        assertThat(UriEncoderDecoder.decodeAndDropAuthority(
                URI.create("https://localhost:9090/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83%D1%80%D0%BE" +
                        "%D0%B2%D0%B5%D0%BD%D1%8C%202%3D/%3F%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0%BE%D0%B1%D0" +
                        "%B5%D0%BB%2B%D0%BF%D0%BB%D1%8E%D1%81")))
                .isEqualTo("https://localhost:9090/уровень1/уровень 2=/?&файл пробел+плюс");
    }

    @Test
    void decodeAndDropAuthorityWithAuthority() {
        assertThat(UriEncoderDecoder.decodeAndDropAuthority(
                URI.create("https://user:password@localhost:9090/%D1%83%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C1/%D1%83" +
                        "%D1%80%D0%BE%D0%B2%D0%B5%D0%BD%D1%8C%202%3D/%3F%26%D1%84%D0%B0%D0%B9%D0%BB%20%D0%BF%D1%80%D0" +
                        "%BE%D0%B1%D0%B5%D0%BB%2B%D0%BF%D0%BB%D1%8E%D1%81")))
                .isEqualTo("https://localhost:9090/уровень1/уровень 2=/?&файл пробел+плюс");
    }
}
