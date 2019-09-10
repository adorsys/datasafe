package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

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
    public DefaultPathEncryptorDecryptor(){
        sivMode = new SivMode();
    }

    @Override
    public byte[] encrypt(SecretKeyIDWithKey secretKey, byte[] rawData) {
        return sivMode.encrypt(secretKey.getCounterSecretKey().getEncoded(),
                               secretKey.getSecretKey().getEncoded(),
                               rawData);
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(SecretKeyIDWithKey secretKey, byte[] encryptedData) {
        return sivMode.decrypt(secretKey.getCounterSecretKey().getEncoded(),
                               secretKey.getSecretKey().getEncoded(),
                               encryptedData);
    }
}
