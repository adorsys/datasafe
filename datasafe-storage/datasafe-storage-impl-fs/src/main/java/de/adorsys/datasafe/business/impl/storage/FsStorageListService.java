package de.adorsys.datasafe.business.impl.storage;

import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FsStorageListService implements StorageListService {

    private final Path tempDir;

    @Inject
    public FsStorageListService(Path tempDir) {
        this.tempDir = tempDir;
    }

    @SneakyThrows
    @Override
    public Stream<PrivateResource> list(ResourceLocation location) {
        return Files.walk(resolve(location.location(), false))
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .map(it -> new DefaultPrivateResource(it.toUri()));
    }

    private Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(tempDir.toUri().resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(tempDir.toUri().resolve(uri));
    }
}
