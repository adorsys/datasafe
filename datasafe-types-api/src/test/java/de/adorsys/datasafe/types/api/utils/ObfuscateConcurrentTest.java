package de.adorsys.datasafe.types.api.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ObfuscateConcurrentTest {

    private static final String TEST_STRING = "/path/to/file";

    @Test
    @SneakyThrows
    void hidingWithHashConcurrencyOk() {
        Obfuscate.secureLogs = ""; // hash

        testLogging();
    }

    @Test
    @SneakyThrows
    void hidingWithStartsConcurrencyOk() {
        Obfuscate.secureLogs = "STARS";

        testLogging();
    }

    @SneakyThrows
    private void testLogging() {
        List<String> results = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.range(0, 100).forEach(it -> executorService.submit(() -> results.add(Obfuscate.secure(TEST_STRING))));
        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);

        // if exception happens size won't be equal to 100
        assertThat(results).hasSize(100);
    }
}
