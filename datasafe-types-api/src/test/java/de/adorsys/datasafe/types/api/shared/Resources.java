package de.adorsys.datasafe.types.api.shared;

import com.google.common.io.MoreFiles;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@UtilityClass
public class Resources {

    @SneakyThrows
    public void copyResourceDir(String resourceDirSrc, Path destination) {
        Path resources = Paths.get(com.google.common.io.Resources.getResource(resourceDirSrc).toURI());

        try (Stream<Path> walk = Files.walk(resources)) {
            walk.forEach(resource -> copyResource(destination, resources, resource));
        }
    }

    @SneakyThrows
    public void copyResource(Path destination, String resourceRoot, String resource) {
        copyResource(
                destination,
                Paths.get(com.google.common.io.Resources.getResource(resourceRoot).toURI()),
                Paths.get(com.google.common.io.Resources.getResource(resource).toURI())
        );
    }

    @SneakyThrows
    public void copyResource(Path destination, Path resourcesRoot, Path resource) {
        Path relative = resourcesRoot.relativize(resource);
        Path inTemp = destination.resolve(relative);
        MoreFiles.createParentDirectories(inTemp);

        if (resource.toFile().isDirectory()) {
            return;
        }

        Files.copy(resource, inTemp);
    }
}
