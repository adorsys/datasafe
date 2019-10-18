package de.adorsys.datasafe.types.api.shared;

import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.condition.OS;

import java.net.URI;
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
                    .map(it -> root.toUri().relativize(it.toUri()).toString().replaceFirst("\\./", ""))
                    .collect(Collectors.toList());
        }
    }

    public String computeRelativePreventingDoubleUrlEncode(Path root, Path child) {
        if (!OS.WINDOWS.isCurrentOs()) {
            return URI.create(root.relativize(child).toString()).getPath();
        }

        // Windows causes double-encoding of URI
        return new Uri(URI.create(root.relativize(child).toString().replace('\\', '/'))).asString();
    }
}
