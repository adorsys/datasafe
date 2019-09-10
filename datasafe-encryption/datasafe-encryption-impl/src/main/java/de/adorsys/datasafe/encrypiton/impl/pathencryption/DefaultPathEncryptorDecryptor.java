package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default path encryption/decryption that uses AES-GCM-SIV mode.
 *
 * @see <a href="https://tools.ietf.org/html/rfc845">RFC-845</a>
 * Using @see <a href="https://github.com/cryptomator/siv-mode">SIV-MODE</a> library for encryption and decryption
 * Encodes resulting bytes using Base64-urlsafe encoding.
 */
@RuntimeDelegate
public class DefaultPathEncryptorDecryptor implements PathEncryptorDecryptor {

    private final SivMode sivMode;

    @Inject
    public DefaultPathEncryptorDecryptor(SivMode sivMode){
        this.sivMode = sivMode;
    }

    @Override
    public String encrypt(PathEncryptionSecretKey pathSecretKey, String originalPath) {
        return new String(sivMode.encrypt(pathSecretKey.getCounterSecretKey().getEncoded(),
                                          pathSecretKey.getSecretKey().getEncoded(),
                                          originalPath.getBytes(UTF_8))
        );
    }

    @Override
    @SneakyThrows
    public String decrypt(PathEncryptionSecretKey pathSecretKey, String encryptedPath) {
        return new String(sivMode.decrypt(pathSecretKey.getCounterSecretKey().getEncoded(),
                                          pathSecretKey.getSecretKey().getEncoded(),
                                          encryptedPath.getBytes(UTF_8))
        );
    }
}
