package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.cryptomator.siv.SivMode;

import javax.inject.Inject;

/**
 * Default path encryption/decryption that uses encryption specified by {@link DefaultPathDigestConfig} and
 * encodes resulting bytes using Base64-urlsafe encoding.
 */
@Slf4j
@RuntimeDelegate
public class DefaultPathEncryptor implements PathEncryptor {

    //TODO add link to RFC and Library on github
    private SivMode sivMode;

    @Inject
    public DefaultPathEncryptor(){
        sivMode = new SivMode();
    }

    @Override
    public byte[] encrypt(SecretKeyIDWithKey secretKeyEntry, byte[] rawData) {
        return sivMode.encrypt(secretKeyEntry.getCounter().getValue(), 
                               secretKeyEntry.getSecretKey().getEncoded(),
                               rawData);
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(SecretKeyIDWithKey secretKey, byte[] encryptedData) {
        return sivMode.decrypt(secretKey.getCounter().getValue(),
                               secretKey.getSecretKey().getEncoded(),
                               encryptedData);
    }
}
