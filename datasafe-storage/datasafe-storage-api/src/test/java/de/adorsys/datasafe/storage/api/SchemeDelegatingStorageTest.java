package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SchemeDelegatingStorageTest extends BaseMockitoTest {

    private static final String PROTOCOL = "protocol";

    private AbsoluteLocation locationExists = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate(PROTOCOL + "://bucket")
    );

    private WithCallback<AbsoluteLocation, ResourceWriteCallback> locationExistsWithCallback =
            WithCallback.noCallback(locationExists);

    private AbsoluteLocation locationNotExists = new AbsoluteLocation<>(
            BasePrivateResource.forPrivate("ZZZZ://bucket")
    );

    private StorageService service = mock(StorageService.class);

    private Map<String, StorageService> storageServices = Collections.singletonMap(
            PROTOCOL,
            service
    );

    private SchemeDelegatingStorage tested = new SchemeDelegatingStorage(storageServices);

    @Test
    void objectExistsDelegates() {
        tested.objectExists(locationExists);

        verify(service).objectExists(locationExists);
    }

    @Test
    void listDelegates() {
        tested.list(locationExists);

        verify(service).list(locationExists);
    }

    @Test
    void readDelegates() {
        tested.read(locationExists);

        verify(service).read(locationExists);
    }

    @Test
    void removeDelegates() {
        tested.remove(locationExists);

        verify(service).remove(locationExists);
    }

    @Test
    void writeDelegates() {
        tested.write(locationExistsWithCallback);

        verify(service).write(locationExistsWithCallback);
    }

    @Test
    void objectExistsFails() {
        assertThrows(IllegalArgumentException.class, () -> tested.objectExists(locationNotExists));

        verify(service, never()).objectExists(any());
    }

    @Test
    void listFails() {
        assertThrows(IllegalArgumentException.class, () -> tested.list(locationNotExists));

        verify(service, never()).list(any());
    }

    @Test
    void readFails() {
        assertThrows(IllegalArgumentException.class, () -> tested.read(locationNotExists));

        verify(service, never()).read(any());
    }

    @Test
    void removeFails() {
        assertThrows(IllegalArgumentException.class, () -> tested.remove(locationNotExists));

        verify(service, never()).remove(any());
    }

    @Test
    void writeFails() {
        assertThrows(IllegalArgumentException.class, () -> tested.write(WithCallback.noCallback(locationNotExists)));

        verify(service, never()).write(any());
    }
}
