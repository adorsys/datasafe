package de.adorsys.datasafe.cli.hacks;

import java.security.SecureRandom;

public class HackSecureRandom extends SecureRandom {

    public HackSecureRandom() {
        super(MyFactory.spi(), MyFactory.provider());
    }
}
