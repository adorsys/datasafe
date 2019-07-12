package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator;

import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Acts as a counter when to stop drools operation generation.
 */
@Getter
@RequiredArgsConstructor
public class HistoryList {

    private final List<Operation> operations;
    private final int maxSize;

    /**
     * This will not hard-enforce operation count, rather only soft limit is enforced
     */
    public boolean canContinue() {
        return operations.size() < maxSize;
    }
}
