package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

/**
 * Default path encryption/decryption that uses encryption specified by {@link DefaultPathDigestConfig} and
 * encodes resulting bytes using Base64-urlsafe encoding.
 */
@RuntimeDelegate
public class DefaultPathEncryptor implements PathEncryptor {

    //TODO add link to RFC and Library on github
    private final SivMode sivMode;

    @Inject
    public DefaultPathEncryptor(){
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
