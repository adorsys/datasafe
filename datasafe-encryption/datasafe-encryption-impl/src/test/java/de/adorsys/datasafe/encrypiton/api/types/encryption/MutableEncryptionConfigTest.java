package de.adorsys.datasafe.encrypiton.api.types.encryption;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MutableEncryptionConfigTest extends BaseMockitoTest {

    @ValueSource(strings = {
            "mutable-scrypt.yaml",
            "mutable-pbkdf2.yaml"
    })
    @ParameterizedTest
    @SneakyThrows
    void mappingTest(String yamlFixture) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String expected = readResource(yamlFixture);
        MutableEncryptionConfig config = readResource(mapper, yamlFixture, MutableEncryptionConfig.class);
        assertThat(config.getKeystore().getType()).isEqualTo("store-type");
        assertThat(mapper.writeValueAsString(config.toEncryptionConfig())).isEqualTo(expected);
    }

    @SneakyThrows
    private <T> T readResource(ObjectMapper mapper, String path, Class<T> type) {
        try (Reader reader = Resources.asCharSource(Resources.getResource(path), StandardCharsets.UTF_8).openStream()) {
            return mapper.readValue(reader, type);
        }
    }

    @SneakyThrows
    private String readResource(String path) {
        CharSource reader = Resources.asCharSource(Resources.getResource(path), StandardCharsets.UTF_8);
        return reader.read();
    }
}