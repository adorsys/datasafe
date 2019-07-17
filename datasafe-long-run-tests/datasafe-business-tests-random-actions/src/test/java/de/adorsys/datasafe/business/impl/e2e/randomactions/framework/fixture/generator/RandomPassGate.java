package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Checks if operation can pass based on probability.
 */
@Slf4j
@RequiredArgsConstructor
public class RandomPassGate {

    private final Random random;

    public boolean canPass(int threshold, String forWhat) {
        int rand = random.nextInt(100);
        log.debug("CAN PASS [{}]? {} < {} : {}", forWhat, rand, threshold, rand < threshold);
        return rand < threshold;
    }
}
