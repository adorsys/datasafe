package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator;

import com.google.common.collect.Iterables;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.TestFileTreeOper;
import lombok.RequiredArgsConstructor;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Generates some random path using path components and filename pools.
 */
@RequiredArgsConstructor
public class RandomPathGenerator {

    public static final List<String> DEFAULT_COMPONENTS = ImmutableList.of("home", "documents", "important");
    public static final List<String> DEFAULT_FILENAMES = ImmutableList.of(
            "file.txt", "document.pdf", "presentation.ppt", "balance.xlsx"
    );

    private final int probabilityOfRandomPath;
    private final int probabilityOfDir;
    private final Random random;
    private final int maxDepth;
    private final List<String> pathComponents;
    private final List<String> filenames;

    public String generate() {
        return randomPath();
    }

    public String generateList(TestFileTreeOper fileSystem) {
        if (random.nextInt(100) < probabilityOfRandomPath) {
            return randomPath();
        }

        return pathFromFs(fileSystem);
    }

    public String generateInbox() {
        return generateFilename();
    }

    private String pathFromFs(TestFileTreeOper fileSystem) {
        if (fileSystem.getFiles().keySet().isEmpty()) {
            return "";
        }

        String path = Iterables.get(
                fileSystem.getFiles().keySet(),
                random.nextInt(fileSystem.getFiles().keySet().size())
        );

        if (random.nextInt(100) > probabilityOfDir) {
            return path;
        }

        int depth = Math.min(path.split("/").length, maxDepth);
        String relPath = IntStream.range(
                0,
                depth > 0 ? random.nextInt(depth) : 0
        ).boxed().map(it -> "..").collect(Collectors.joining("/"));

        return URI.create(path).resolve(relPath).toASCIIString();
    }

    private String randomPath() {
        List<String> components = Stream.of(generatePath(), generateFilename())
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toList());

        return String.join("/", components);
    }

    private String generatePath() {
        List<String> components = new ArrayList<>();
        IntStream.range(0, random.nextInt(maxDepth)).forEach(it ->
                components.add(pathComponents.get(random.nextInt(pathComponents.size())))
        );

        return String.join("/", components);
    }

    private String generateFilename() {
        return filenames.get(random.nextInt(filenames.size()));
    }
}
