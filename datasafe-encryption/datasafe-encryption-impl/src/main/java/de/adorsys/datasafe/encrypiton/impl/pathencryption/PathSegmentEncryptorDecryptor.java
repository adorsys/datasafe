package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

/**
 * Default path encryption/decryption that uses AES-SIV mode.
 *
 * @see <a href="https://tools.ietf.org/html/rfc845">RFC-845</a>
 * Using @see <a href="https://github.com/cryptomator/siv-mode">SIV-MODE</a> library for encryption and decryption
 * Encodes resulting bytes using Base64-urlsafe encoding.
 */
@Slf4j
@RuntimeDelegate
public class PathSegmentEncryptorDecryptor implements PathEncryptorDecryptor {

    private final SivMode sivMode;

    @Inject
    public PathSegmentEncryptorDecryptor(SivMode sivMode) {
        this.sivMode = sivMode;
    }

    @Override
    public byte[] encrypt(AuthPathEncryptionSecretKey pathSecretKey, byte[] originalPath, byte[] associated) {

        return sivMode.encrypt(
                pathSecretKey.getCounterSecretKey().getSecretKey(),
                pathSecretKey.getSecretKey().getSecretKey(),
                originalPath,
                associated
        );
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(AuthPathEncryptionSecretKey pathSecretKey, byte[] encryptedPath, byte[] associated) {

        return sivMode.decrypt(
                pathSecretKey.getCounterSecretKey().getSecretKey(),
                pathSecretKey.getSecretKey().getSecretKey(),
                encryptedPath,
                associated
        );
    }
}
