package de.adorsys.datasafe.business.impl.storage;

import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.business.api.storage.StorageService;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.utils.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final URI dir;

    @SneakyThrows
    @Override
    public Stream<AbsoluteResourceLocation<PrivateResource>> list(AbsoluteResourceLocation path) {
        log.debug("List file request: {}", path);
        Path filePath = resolve(path.location(), false);
        log.debug("List file: {}", Log.secure(filePath));

        // FS should be compatible with s3 behavior:
        if (!filePath.toFile().exists()) {
            return Stream.empty();
        }

        return Files.walk(filePath)
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .map(it -> new AbsoluteResourceLocation<>(new DefaultPrivateResource(it.toUri())));
    }

    @SneakyThrows
    @Override
    public InputStream read(AbsoluteResourceLocation path) {
        log.debug("Read file request: {}", path);
        Path filePath = resolve(path.location(), false);
        log.debug("Read file: {}", Log.secure(filePath));
        return MoreFiles.asByteSource(filePath, StandardOpenOption.READ).openStream();
    }

    @SneakyThrows
    @Override
    public OutputStream write(AbsoluteResourceLocation path) {
        log.debug("Write file request: {}", Log.secure(path.location()));
        Path filePath = resolve(path.location(), true);
        log.debug("Write file: {}", Log.secure(filePath));
        return MoreFiles.asByteSink(filePath, StandardOpenOption.CREATE).openStream();
    }

    @SneakyThrows
    @Override
    public void remove(AbsoluteResourceLocation location) {
        Files.delete(resolve(location.location(), false));
        log.debug("deleted directory at: {}", location);
    }

    @Override
    public boolean objectExists(AbsoluteResourceLocation location) {
        boolean exists = Files.exists(resolve(location.location(), false));
        log.debug("exists {} directory at: {}", exists, Log.secure(location));
        return exists;
    }

    protected Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(dir.resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            log.debug("Creating directories at: {}", Log.secure(path));
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(dir.resolve(uri));
    }
}
