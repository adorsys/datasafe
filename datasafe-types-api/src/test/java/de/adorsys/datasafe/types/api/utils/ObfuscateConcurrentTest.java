package de.adorsys.datasafe.types.api.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ObfuscateConcurrentTest {

    private static final String TEST_STRING = "/path/to/file";

    @Test
    @SneakyThrows
    void hidingWithHashConcurrencyOk() {
        Obfuscate.secureLogs = ""; // hash

        testLoggingStaticString();
        testLoggingDynamicString();
    }

    @Test
    @SneakyThrows
    void hidingWithStartsConcurrencyOk() {
        Obfuscate.secureLogs = "STARS";

        testLoggingStaticString();
        testLoggingDynamicString();
    }

    @SneakyThrows
    private void testLoggingStaticString() {
        testLogging(() -> TEST_STRING);
    }

    @SneakyThrows
    private void testLoggingDynamicString() {
        int size = ThreadLocalRandom.current().nextInt(10, 100);
        byte[] bytes = new byte[size];
        ThreadLocalRandom.current().nextBytes(bytes);
        testLogging(() -> new String(bytes, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private void testLogging(Supplier<String> stringToEncryptSupplier) {
        List<String[]> results = new CopyOnWriteArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.range(0, 1000).forEach(
                it -> executorService.submit(() -> {
                    String data = stringToEncryptSupplier.get();
                    results.add(new String[] {data, Obfuscate.secure(data)});
                })
        );

        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();

        // parallel encryption yields same as sequential
        assertThat(results).allMatch(it -> it[1].equals(Obfuscate.secure(it[0])));
        // if exception happens size won't be equal to 1000
        assertThat(results).hasSize(1000);
    }
}
