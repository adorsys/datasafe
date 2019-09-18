package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

/**
 * Default path encryption/decryption that uses AES-GCM-SIV mode.
 *
 * @see <a href="https://tools.ietf.org/html/rfc845">RFC-845</a>
 * Using @see <a href="https://github.com/cryptomator/siv-mode">SIV-MODE</a> library for encryption and decryption
 * Encodes resulting bytes using Base64-urlsafe encoding.
 */
@Slf4j
@RuntimeDelegate
public class DefaultPathEncryptorDecryptor implements PathEncryptorDecryptor {

    private final SivMode sivMode;

    @Inject
    public DefaultPathEncryptorDecryptor(SivMode sivMode) {
        if(sivMode == null) {
            throw new IllegalArgumentException("SivMode must be provided for encryption and decryption");
        }
        this.sivMode = sivMode;
    }

    @Override
    public byte[] encrypt(PathEncryptionSecretKey pathSecretKey, byte[] originalPath, byte[] associated) {
        log.debug("Path secret key: {}, original path: {}", pathSecretKey.toString(), originalPath);

        return sivMode.encrypt(
                pathSecretKey.getCounterSecretKey().getEncoded(),
                pathSecretKey.getSecretKey().getEncoded(),
                originalPath,
                associated
        );
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(PathEncryptionSecretKey pathSecretKey, byte[] encryptedPath, byte[] associated) {
        log.debug("Path secret key: {}, encrypted path: {}", pathSecretKey.toString(), encryptedPath);

        return sivMode.decrypt(
                pathSecretKey.getCounterSecretKey().getEncoded(),
                pathSecretKey.getSecretKey().getEncoded(),
                encryptedPath,
                associated
        );
    }
}
