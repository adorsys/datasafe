package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDbUriExtractorTest extends BaseMockitoTest {

    @Test
    void validateExtract() {
        String dbConnUri = new DefaultDbUriExtractor().extract(
                BasePrivateResource.forAbsolutePrivate(
                        URI.create("jdbc-mysql://sa:pazzword@localhost:3306/database/tablename")
                )
        );

        assertThat(dbConnUri).isEqualTo("jdbc:mysql://localhost:3306/database");
    }
}