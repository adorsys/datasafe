package de.adorsys.datasafe.business.impl.e2e;

import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
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
public class FileSystemStorage {

    private final Path dir;

    @SneakyThrows
    protected Stream<PrivateResource> listFiles(ResourceLocation path) {
        log.debug("List file request: {}", path.location());
        Path filePath = resolve(path.location(), false);
        log.debug("List file: {}", filePath);

        return Files.walk(filePath)
                .filter(it -> !it.startsWith("."))
                .filter(it -> !it.toFile().isDirectory())
                .map(it -> new DefaultPrivateResource(it.toUri()));
    }

    @SneakyThrows
    protected InputStream readFile(ResourceLocation path) {
        log.debug("Read file request: {}", path.location());
        Path filePath = resolve(path.location(), false);
        log.debug("Read file: {}", filePath);
        return MoreFiles.asByteSource(filePath, StandardOpenOption.READ).openStream();
    }

    @SneakyThrows
    protected OutputStream writeFile(ResourceLocation path) {
        log.debug("Write file request: {}", path.location());
        Path filePath = resolve(path.location(), true);
        log.debug("Write file: {}", filePath);
        return MoreFiles.asByteSink(filePath, StandardOpenOption.CREATE).openStream();
    }

    protected Path resolve(URI uri, boolean mkDirs) {
        Path path = Paths.get(dir.toUri().resolve(uri));
        if (!path.getParent().toFile().exists() && mkDirs) {
            log.debug("Creating directories at: {}", path);
            path.getParent().toFile().mkdirs();
        }

        return Paths.get(dir.toUri().resolve(uri));
    }
}
