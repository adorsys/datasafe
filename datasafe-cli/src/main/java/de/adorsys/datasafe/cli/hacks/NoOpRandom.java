package de.adorsys.datasafe.cli.hacks;

import java.security.SecureRandom;

public class NoOpRandom extends SecureRandom {

    public NoOpRandom() {
        super(null, null);
    }
}
