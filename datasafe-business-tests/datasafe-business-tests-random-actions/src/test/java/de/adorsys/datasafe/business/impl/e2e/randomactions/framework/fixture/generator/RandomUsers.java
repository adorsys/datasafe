package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Checks if operation can pass based on probability.
 */
@Slf4j
@RequiredArgsConstructor
public class RandomUsers {

    private final Random random;

    public int randomUserCount(int min, int max) {
        log.debug("RAND User count {}", min);

        if (max == min) {
            return min;
        }

        return min + random.nextInt(max - min);
    }
}
