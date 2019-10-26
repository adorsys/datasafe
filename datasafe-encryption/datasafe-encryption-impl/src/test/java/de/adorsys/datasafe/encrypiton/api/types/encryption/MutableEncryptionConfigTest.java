package de.adorsys.datasafe.encrypiton.api.types.encryption;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MutableEncryptionConfigTest extends BaseMockitoTest {

    private ObjectMapper mapper = createMapper();

    @ValueSource(strings = {
            "config-test/mutable-scrypt.yaml",
            "config-test/mutable-pbkdf2.yaml"
    })
    @ParameterizedTest
    @SneakyThrows
    void mappingTest(String yamlFixture) {
        String expected = readResource(yamlFixture);
        MutableEncryptionConfig config = readResource(mapper, yamlFixture, MutableEncryptionConfig.class);
        assertThat(config.getKeystore().getType()).isEqualTo("store-type");
        assertThat(mapper.writeValueAsString(config.toEncryptionConfig())).isEqualTo(expected);
    }

    @MethodSource("nullsTest")
    @ParameterizedTest
    @SneakyThrows
    void mappingWithNullsTest(SourceAndExpectation yamlFixture) {
        String expected = readResource(yamlFixture.getExpectation());
        MutableEncryptionConfig config = readResource(mapper, yamlFixture.getSource(), MutableEncryptionConfig.class);
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

    private static Stream<SourceAndExpectation> nullsTest() {
        return Stream.of(
                new SourceAndExpectation(
                        "config-test/null-test/mutable-null-cms.yaml",
                        "config-test/null-test/expectation/mutable-null-cms.yaml"
                ),
                new SourceAndExpectation(
                        "config-test/null-test/mutable-null-keys.yaml",
                        "config-test/null-test/expectation/mutable-null-keys.yaml"
                ),
                new SourceAndExpectation(
                        "config-test/null-test/mutable-null-keystore.yaml",
                        "config-test/null-test/expectation/mutable-null-keystore.yaml"
                )
        );
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Data
    @AllArgsConstructor
    private static class SourceAndExpectation {

        private String source;
        private String expectation;
    }
}