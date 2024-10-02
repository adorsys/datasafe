package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.OutputStream;
import java.util.Optional;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserBasedDelegatingStorageTest extends BaseMockitoTest {
    @Mock
    private StorageService storage;
    @Mock
    private Function<String, StorageService> storageServiceBuilder;
    private UserBasedDelegatingStorage tested;
    private static final List<String> AMAZON_BUCKETS = List.of("bucket1", "bucket2");

    private AbsoluteLocation locationUser1 = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate("s3://datasafe-test1/073047da-dd68-4f70-b9bf-5759d7e30c85/users/user-1/private/files/")
    );
    private AbsoluteLocation locationUser3 = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate("s3://datasafe-test1/073047da-dd68-4f70-b9bf-5759d7e30c85/users/user-3/private/files/")
    );
    private AbsoluteLocation invalidLocation = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate("invalid://path")
    );

    @BeforeEach
    void init() {
        when(storageServiceBuilder.apply(any())).thenReturn(storage);
        tested = new UserBasedDelegatingStorage(storageServiceBuilder, AMAZON_BUCKETS);
    }

    @Test
    void serviceUser1() {
        tested.objectExists(locationUser1);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).objectExists(locationUser1);
    }

    @Test
    void serviceUser3() {
        tested.objectExists(locationUser3);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).objectExists(locationUser3);
    }

    @Test
    void serviceInvalidLocation() {
        assertThrows(IllegalStateException.class, () -> tested.objectExists(invalidLocation));

        verify(storageServiceBuilder, never()).apply(any());
        verify(storage, never()).objectExists(any());
    }

    @Test
    void flushChunkSizeUser1() {
        Optional<Integer> chunkSize = tested.flushChunkSize(locationUser1);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).flushChunkSize(locationUser1);
        assertThat(chunkSize).isEmpty();
    }

    @Test
    void flushChunkSizeUser3() {
        Optional<Integer> chunkSize = tested.flushChunkSize(locationUser3);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).flushChunkSize(locationUser3);
        assertThat(chunkSize).isEmpty();
    }

    @Test
    void listDelegates() {
        tested.list(locationUser1);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).list(locationUser1);
    }

    @Test
    void readDelegates() {
        tested.read(locationUser1);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).read(locationUser1);
    }

    @Test
    void removeDelegates() {
        tested.remove(locationUser1);

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).remove(locationUser1);
    }

    @Test
    void writeDelegates() {
        tested.write(WithCallback.noCallback(locationUser1));

        verify(storageServiceBuilder).apply("bucket2");
        verify(storage).write(any(WithCallback.class));
    }

    @Test
    void defaultFlushChunkSize() {

        StorageWriteService defaultStorageWriteService = new StorageWriteService() {
            @Override
            public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
                return null;
            }
        };
        Optional<Integer> chunkSize = defaultStorageWriteService.flushChunkSize(locationUser1);
        assertThat(chunkSize).isEmpty();
    }
}
