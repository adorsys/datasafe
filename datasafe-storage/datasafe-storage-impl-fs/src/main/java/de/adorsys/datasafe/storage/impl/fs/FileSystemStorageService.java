package de.adorsys.datasafe.storage.impl.fs;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocationWithCapability;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BaseResolvedResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.StorageCapability;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.resource.UriEncoderDecoder;
import de.adorsys.datasafe.types.api.resource.WithCallback;
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

        boolean shouldReturnDir = shouldReturnDir(path);
        return Files.walk(filePath)
                // filter directories out based on setting
                .filter(it -> (shouldReturnDir && allowableDir(it)) || !it.toFile().isDirectory())
                .map(it -> new AbsoluteLocation<>(
                        new BaseResolvedResource(
                                // We store path in uri-encoded form, so toUri calls will fail
                                new BasePrivateResource(
                                        new Uri(URI.create(UriEncoderDecoder.decodeAndDropAuthority(it.toUri())))
                                ),
                                Instant.ofEpochMilli(it.toFile().lastModified()))
                        )
                );
    }

    private boolean allowableDir(Path it) {
        String name = it.getFileName().toString();
        return !".".equals(name)
                && !"..".equals(name)
                // prevents root folder to appear
                && !(it.toString() + "/").equals(dir.getRawPath());
    }

    private boolean shouldReturnDir(AbsoluteLocation path) {
        return path instanceof AbsoluteLocationWithCapability
                && ((AbsoluteLocationWithCapability) path).getCapability().equals(StorageCapability.LIST_RETURNS_DIR);
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
        return MoreFiles
                .asByteSink(filePath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
                .openStream();
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
