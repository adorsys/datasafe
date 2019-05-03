package de.adorsys.datasafe.business.impl.storage;

import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FsStorageWriteService implements StorageWriteService {

    private final Path tempDir;

    @Inject
    public FsStorageWriteService(Path tempDir) {
        this.tempDir = tempDir;
    }

    @SneakyThrows
    @Override
    public OutputStream write(ResourceLocation location) {
        Path filePath = resolve(location.location(), true);
        return MoreFiles.asByteSink(filePath, StandardOpenOption.CREATE).openStream();
    }

    private Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(tempDir.toUri().resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(tempDir.toUri().resolve(uri));
    }
}
