package de.adorsys.datasafe.business.impl.e2e.performance.fixture.generator;

import lombok.RequiredArgsConstructor;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public String generate() {
        List<String> components = Stream.of(generatePath(), generateFilename())
                .filter(it -> !it.isEmpty())
                .collect(Collectors.toList());

        return "./" + String.join("/", components);
    }

    public String generateInbox() {
        return "./" + generateFilename();
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
