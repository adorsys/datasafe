package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VersionedPrivateResourceTest extends BaseMockitoTest {

    private static final String SOME_PATH = "some/path/";
    private Version version = mock(Version.class);

    private PrivateResource resource = BasePrivateResource.forPrivate(SOME_PATH);
    private PrivateResource absoluteResource = BasePrivateResource
            .forAbsolutePrivate("file://some/path/").getResource();

    private VersionedPrivateResource<Version> tested = new VersionedPrivateResource<>(resource, version);
    private VersionedPrivateResource<Version> absoluteTested = new VersionedPrivateResource<>(absoluteResource, version);

    @Test
    void encryptedPath() {
        assertThat(tested.encryptedPath().asString()).isEqualTo(SOME_PATH);
    }

    @Test
    void decryptedPath() {
        assertThat(tested.decryptedPath().asString()).isEqualTo("");
    }

    @Test
    void location() {
        assertThat(tested.location().asString()).isEqualTo(SOME_PATH);
    }

    @Test
    void resolveFrom() {
        assertThat(
                tested.resolveFrom(BasePrivateResource.forAbsolutePrivate("file://data")).location().asString()
        ).isEqualTo("file://data/some/path/");
    }

    @Test
    void resolve() {
        assertThat(
                absoluteTested.resolve(new Uri("aa"), new Uri("bb")).location().asString()
        ).isEqualTo("file://some/path/aa");
    }

    @Test
    void withAuthority() {
        assertThat(
                absoluteTested.withAuthority("user", "secret").location().toASCIIString()
        ).isEqualTo("file://user:secret@some/path/");
    }

    @Test
    void getResource() {
        assertThat(tested.getResource()).isEqualTo(resource);
    }

    @Test
    void getVersion() {
        assertThat(tested.getVersion()).isEqualTo(version);
    }
}
