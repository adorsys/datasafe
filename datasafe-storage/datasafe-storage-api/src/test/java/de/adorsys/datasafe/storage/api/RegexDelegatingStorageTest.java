package de.adorsys.datasafe.storage.api;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class RegexDelegatingStorageTest extends BaseMockitoTest {

    @Mock
    private StorageService service;
    private RegexDelegatingStorage tested;
    private AbsoluteLocation location;

    @BeforeEach
    void setUp() {
        Map<Pattern, StorageService> storageByPattern = Collections.singletonMap(Pattern.compile("s3://.*"), service);
        tested = new RegexDelegatingStorage(storageByPattern);
        location = new AbsoluteLocation<>(BasePrivateResource.forPrivate("s3://bucket"));
    }
    @Test
    void objectExists() {
        tested.objectExists(location);
        verify(service).objectExists(location);
    }
    @Test
    void list() {
        tested.list(location);
        verify(service).list(location);
    }
    @Test
    void read() {
        tested.read(location);
        verify(service).read(location);
    }
    @Test
    void remove() {
        tested.remove(location);
        verify(service).remove(location);
    }
    @Test
    void write() {
        tested.write(WithCallback.noCallback(location));
        verify(service).write(any(WithCallback.class));
    }
    @Test
        void objectExistsWithNoMatch() {
        AbsoluteLocation badlocation = new AbsoluteLocation<>(BasePrivateResource.forPrivate("file://bucket"));
        assertThrows(IllegalArgumentException.class, () -> tested.objectExists(badlocation));
    }

}
