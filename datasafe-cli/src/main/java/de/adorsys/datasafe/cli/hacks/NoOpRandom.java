package de.adorsys.datasafe.cli.hacks;

import java.security.SecureRandom;

/**
 * This class substitutes SecureRandom during CLI generation, it should be overridden during runtime.
 */
public class NoOpRandom extends SecureRandom {

    public NoOpRandom() {
        super(null, null);
    }
}
