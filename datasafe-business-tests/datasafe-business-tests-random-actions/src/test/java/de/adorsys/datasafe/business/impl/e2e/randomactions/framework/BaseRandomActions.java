package de.adorsys.datasafe.business.impl.e2e.randomactions.framework;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseRandomActions extends WithStorageProvider {

    private static final Set<Integer> threadCount = ImmutableSet.of(4, 8);
    private static final Set<Integer> fileSizeMBytes = ImmutableSet.of(1, 10);

    protected static Fixture fixture;

    /**
     * Initializes {@link Fixture} from performance/fixture/fixture.json resource file.
     */
    @BeforeAll
    @SneakyThrows
    static void init() {
        try (Reader reader = Resources.asCharSource(
                Resources.getResource("fixture/fixture.json"),
                StandardCharsets.UTF_8).openStream()) {
            fixture = new Gson().fromJson(reader, Fixture.class);
        }
    }

    @ValueSource
    protected static Stream<Arguments> actionsOnAllSoragesAndThreadsAndFilesizes() {
        return Sets.cartesianProduct(
                allDefaultStorages().collect(Collectors.toSet()),
                threadCount,
                fileSizeMBytes
        ).stream().map(it -> Arguments.of(it.get(0), it.get(1), it.get(2)));
    }
}
