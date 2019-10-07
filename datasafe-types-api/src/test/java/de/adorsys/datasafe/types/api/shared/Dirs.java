package de.adorsys.datasafe.types.api.shared;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class Dirs {

    @SneakyThrows
    public List<String> walk(Path root) {
        return walk(root, Integer.MAX_VALUE);
    }

    @SneakyThrows
    public List<String> walk(Path root, int depth) {
        try (Stream<Path> walk = Files.walk(root, depth)) {
            return walk
                    .filter(it -> !(it.getFileName().startsWith(".") || it.getFileName().startsWith("..")))
                    .filter(it -> !it.equals(root))
                    .map(it -> computeRelative(root, it)
                    .replaceFirst("\\./", "")
                    .replaceAll("/$", ""))
                    .collect(Collectors.toList());
        }
    }

    private String computeRelative(Path root, Path child) {
        if (!OS.WINDOWS.isCurrentOs()) {
            return root.toUri().relativize(child.toUri()).toString();
        }

        // Windows causes double-encoding of URI
        return root.relativize(child).toString().replace('\\', '/');
    }
}