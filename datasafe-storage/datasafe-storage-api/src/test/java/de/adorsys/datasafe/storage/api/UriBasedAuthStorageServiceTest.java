package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.net.URI;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UriBasedAuthStorageServiceTest extends BaseMockitoTest {

    @Mock
    private StorageService storage;

    @Mock
    Function<UriBasedAuthStorageService.AccessId, StorageService> getService;

    @Captor
    private ArgumentCaptor<UriBasedAuthStorageService.AccessId> argumentCaptor;

    private UriBasedAuthStorageService tested;

    @BeforeEach
    void init() {
        when(getService.apply(argumentCaptor.capture())).thenReturn(storage);
        tested = new UriBasedAuthStorageService(getService);
    }

    @MethodSource("fixture")
    @ParameterizedTest
    void objectExists(MappedItem item) {
        tested.objectExists(item.getUri());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(item.getAccessId());
        verify(storage).objectExists(item.getUri());
    }

    @MethodSource("fixture")
    @ParameterizedTest
    void list(MappedItem item) {
        tested.list(item.getUri());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(item.getAccessId());
        verify(storage).list(item.getUri());
    }

    @MethodSource("fixture")
    @ParameterizedTest
    void read(MappedItem item) {
        tested.read(item.getUri());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(item.getAccessId());
        verify(storage).read(item.getUri());
    }

    @MethodSource("fixture")
    @ParameterizedTest
    void remove(MappedItem item) {
        tested.remove(item.getUri());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(item.getAccessId());
        verify(storage).remove(item.getUri());
    }

    @MethodSource("fixture")
    @ParameterizedTest
    void write(MappedItem item) {
        tested.write(WithCallback.noCallback(item.getUri()));
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(item.getAccessId());
        verify(storage).write(any());
    }

    @ValueSource
    private static Stream<MappedItem> fixture() {
        return Stream.of(
            new MappedItem(
                "http://user:password@host:9999/region/bucket",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host:9999/",
                    URI.create("http://host:9999/region/bucket"),
                    URI.create("http://host:9999")
                )
            ),
            new MappedItem(
                "http://user:password@host:9999/region/bucket/",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host:9999/",
                    URI.create("http://host:9999/region/bucket/"),
                    URI.create("http://host:9999")
                )
            ),
            new MappedItem(
                "http://user:password@host:9999/region/bucket/path/to",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host:9999/",
                    URI.create("http://host:9999/region/bucket/path/to"),
                    URI.create("http://host:9999")
                )
            ),
            new MappedItem(
                "http://user:password@host:9999/region/bucket/path/to/",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host:9999/",
                    URI.create("http://host:9999/region/bucket/path/to/"),
                    URI.create("http://host:9999")
                )
            ),
            new MappedItem(
                "http://user:password@host.com/region/bucket",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host.com/",
                    URI.create("http://host.com/region/bucket"),
                    URI.create("http://host.com")
                )
            ),
            new MappedItem(
                "http://user:password@host.com/region/bucket/",
                new UriBasedAuthStorageService.AccessId(
                    "user",
                    "password",
                        "region",
                    "bucket",
                    "http://host.com/",
                    URI.create("http://host.com/region/bucket/"),
                    URI.create("http://host.com")
                )
            )
        );
    }

    @Getter
    @ToString(of = "uriAsString")
    @RequiredArgsConstructor
    private static class MappedItem {

        private final String uriAsString;
        private final AbsoluteLocation uri;
        private final UriBasedAuthStorageService.AccessId accessId;

        private MappedItem(String uri, UriBasedAuthStorageService.AccessId accessId) {
            this.uriAsString = uri;
            this.uri = BasePrivateResource.forAbsolutePrivate(uri);
            this.accessId = accessId;
        }
    }
}
