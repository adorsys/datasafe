package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services;

import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.OperationType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.google.common.math.Quantiles.percentiles;

/**
 * Calculates performance statistics (percentile-based) by operation (read/write...).
 */
@Slf4j
@RequiredArgsConstructor
public class StatisticService {

    private final Map<OperationType, List<Integer>> performanceInMsByOp = new ConcurrentHashMap<>();

    public void reportOperationPerformance(Operation oper, int duration) {
        performanceInMsByOp.computeIfAbsent(oper.getType(), id -> new CopyOnWriteArrayList<>()).add(duration);
    }

    public Map<OperationType, Percentiles> generateReport() {
        return performanceInMsByOp.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                it -> new Percentiles(it.getValue())
        ));
    }

    public String reportAsJson(String name) {
        return new Gson().toJson(ImmutableMap.of(name, generateReport()));
    }

    @Data
    public static class Percentiles {

        private final Map<Integer, Double> stat;
        private final double throughputPerThread;

        private Percentiles(List<Integer> values) {
            this.stat = percentiles().indexes(50, 75, 90, 95, 99).compute(values);
            // note that time is in ms, so we scale 1k coef.
            this.throughputPerThread = ((double) values.size() * 1000.0) / values.stream().mapToDouble(it -> it).sum();
        }
    }
}
