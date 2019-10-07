package de.adorsys.datasafe.types.api.shared;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

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
                    .map(it -> root.relativize(it).toString().replaceFirst("\\./", ""))
                    .collect(Collectors.toList());
        }
    }
}
