package de.adorsys.datasafe.business.impl.e2e;

import de.adorsys.datasafe.business.api.config.DFSConfig;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeServices;
import de.adorsys.datasafe.business.impl.storage.FileSystemStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;

public class FsTest extends BaseStorageTest {

    @BeforeEach
    void init(@TempDir Path location) {
        this.location = location.toUri();
        this.storage = new FileSystemStorageService(this.location);

        this.services = DaggerDefaultDocusafeServices
                .builder()
                .config(new DFSConfig() {
                    @Override
                    public String keystorePassword() {
                        return "PAZZWORD";
                    }

                    @Override
                    public URI systemRoot() {
                        return location.toUri();
                    }
                })
                .storageList(storage)
                .storageRead(storage)
                .storageWrite(storage)
                .storageRemove(storage)
                .build();

        loadReport.add("Test with File System");
    }
}
