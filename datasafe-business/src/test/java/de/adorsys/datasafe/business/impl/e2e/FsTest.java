package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class FsTest extends StorageTest {

    void init(@TempDir Path location) {
        this.location = location.toUri();
        this.storage = new FileSystemStorageService(this.location);

        this.services = DaggerDefaultDocusafeServices
                .builder()
                .storageList(storage)
                .storageRead(storage)
                .storageWrite(storage)
                .storageRemove(storage)
                .build();
    }
}
