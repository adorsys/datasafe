package de.adorsys.datasafe.business.impl.e2e.performance.fixture.generator;

import lombok.RequiredArgsConstructor;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

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

    private final Random random;
    private final int maxDepth;
    private final List<String> pathComponents;
    private final List<String> filenames;
    private final List<String> extendedFilenames;

    public RandomPathGenerator(Random random, int maxDepth, List<String> pathComponents, List<String> filenames) {
        this.random = random;
        this.maxDepth = maxDepth;
        this.pathComponents = pathComponents;
        this.filenames = filenames;
        this.extendedFilenames = new ArrayList<>(filenames);
        extendedFilenames.add("");
    }

    public String generate() {
        List<String> components = Stream.of(generatePath(), generateFilename(filenames))
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toList());

        return "./" + String.join("/", components);
    }

    public String generateForList() {
        List<String> components = Stream.of(generatePath(), generateFilename(new ArrayList<>(extendedFilenames)))
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toList());

        return "./" + String.join("/", components);
    }

    public String generateInbox() {
        return "./" + generateFilename(filenames);
    }

    private String generatePath() {
        List<String> components = new ArrayList<>();
        IntStream.range(0, random.nextInt(maxDepth)).forEach(it ->
                components.add(pathComponents.get(random.nextInt(pathComponents.size())))
        );

        return String.join("/", components);
    }

    private String generateFilename(List<String> allowedFilenames) {
        return allowedFilenames.get(random.nextInt(allowedFilenames.size()));
    }
}
