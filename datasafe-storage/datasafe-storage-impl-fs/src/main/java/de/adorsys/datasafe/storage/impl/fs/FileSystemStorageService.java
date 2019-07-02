package de.adorsys.datasafe.storage.impl.fs;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
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
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Filesystem ({@link java.nio.file}) compatible storage service default implementation.
 * Note: Stores paths in URL-encoded form.
 */
@Slf4j
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final Uri dir;

    public FileSystemStorageService(Path dir) {
        this.dir = new Uri(dir.toUri());
    }

    /**
     * Lists resources and returns their location without access credentials.
     */
    @SneakyThrows
    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation path) {
        log.debug("List file request: {}", path.location());
        Path filePath = resolve(path.location().getRawPath(), false);

        // FS should be compatible with s3 behavior:
        if (!filePath.toFile().exists()) {
            return Stream.empty();
        }

        return Files.walk(filePath)
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .map(it -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                // We store path in uri-encoded form, so toUri calls will fail
                                new BasePrivateResource(
                                        new Uri(URI.create("file://" + it.toFile().getAbsolutePath()))
                                ),
                                Instant.ofEpochMilli(it.toFile().lastModified()))
                        )
                );
    }

    @SneakyThrows
    @Override
    public InputStream read(AbsoluteLocation path) {
        log.debug("Read file request: {}",path.location());
        Path filePath = resolve(path.location().getRawPath(), false);
        return MoreFiles.asByteSource(filePath, StandardOpenOption.READ).openStream();
    }

    @SneakyThrows
    @Override
    public OutputStream write(WithCallback<AbsoluteLocation, ? extends ResourceWriteCallback> locationWithCallback) {
        log.debug("Write file request: {}", locationWithCallback.getWrapped().location());
        Path filePath = resolve(locationWithCallback.getWrapped().location().getRawPath(), true);
        log.debug("Write file: {}", locationWithCallback.getWrapped().location());
        return MoreFiles.asByteSink(filePath, StandardOpenOption.CREATE).openStream();
    }

    @SneakyThrows
    @Override
    public void remove(AbsoluteLocation location) {
        if (!objectExists(location)) {
            log.debug("nothing to delete {}", location.location());
            return;
        }

        Path path = resolve(location.location().getRawPath(), false);
        boolean isFile = !path.toFile().isDirectory();
        MoreFiles.deleteRecursively(path, RecursiveDeleteOption.ALLOW_INSECURE);
        log.debug("deleted {} at: {}", isFile ? "file" : "directory", location.location());
    }

    @Override
    public boolean objectExists(AbsoluteLocation location) {
        boolean exists = Files.exists(resolve(location.location().getRawPath(), false));
        log.debug("Exists {}: {}", location.location(), exists);
        return exists;
    }

    protected Path resolve(String uriPath, boolean mkDirs) {
        Path path = Paths.get(dir.resolve(uriPath).asURI());
        if (!path.getParent().toFile().exists() && mkDirs) {
            log.debug("Creating directories for: {}", uriPath);
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(dir.resolve(uriPath).asURI());
    }
}
