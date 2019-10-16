package de.adorsys.datasafe.cli.hacks;

import lombok.SneakyThrows;
import org.bouncycastle.util.test.FixedSecureRandom;

import java.lang.reflect.Field;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;

public class MyFactory {
    private static final SecureRandom RANDOM = init();

    @SneakyThrows
    static SecureRandomSpi spi() {
        System.out.println("SPI ?!!!");
        Field field = SecureRandom.class.getDeclaredField("secureRandomSpi");
        field.setAccessible(true);
        return (SecureRandomSpi) field.get(RANDOM);
    }

    static Provider provider() {
        System.out.println("PROVIDER ?!!!");
        return RANDOM.getProvider();
    }

    static SecureRandom init() {
        if ("ZUMBA".equals(System.getProperty("RUMBA"))) {
            System.out.println("Should not see it!");
            return new FixedSecureRandom(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 8});
        } else {
            System.out.println("Correct one!");
            return new SecureRandom();
        }
    }

    private static class NoopSecureRandom extends SecureRandom {
        public NoopSecureRandom() {
            super(null, null);
        }
    }
}
