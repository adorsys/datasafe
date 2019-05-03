package de.adorsys.datasafe.business.impl.storage;

import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FsStorageReadService implements StorageReadService {

    private final Path tempDir;

    @Inject
    public FsStorageReadService(Path tempDir) {
        this.tempDir = tempDir;
    }

    @SneakyThrows
    @Override
    public InputStream read(ResourceLocation location) {
        return MoreFiles.asByteSource(resolve(location.location(), false), StandardOpenOption.READ).openStream();
    }

    private Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(tempDir.toUri().resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(tempDir.toUri().resolve(uri));
    }
}
