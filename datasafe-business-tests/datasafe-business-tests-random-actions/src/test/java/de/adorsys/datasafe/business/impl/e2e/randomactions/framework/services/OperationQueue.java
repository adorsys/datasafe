package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Fixture;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the source of all operations.
 */
@Slf4j
@RequiredArgsConstructor
public class OperationQueue {

    // AtomicInteger only because of nice interface, no concurrency is expected for value.
    private final Map<String, AtomicInteger> threadIdToFixturePosition;

    private final Fixture fixture;

    public OperationQueue(Fixture fixture) {
        this.fixture = fixture;
        threadIdToFixturePosition = new ConcurrentHashMap<>();
    }

    // sequential from single thread view perspective
    public Operation get(String threadId) {
        int pos = threadIdToFixturePosition.computeIfAbsent(threadId, id -> new AtomicInteger()).getAndIncrement();
        if (pos < fixture.getOperations().size()) {
            return fixture.getOperations().get(pos);
        }

        return null;
    }
}
