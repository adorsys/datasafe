package de.adorsys.datasafe.simple.adapter.impl.cmsencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.ASNCmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class SwitchableCmsEncryptionImpl extends CMSEncryptionServiceImpl {
    public static final String NO_CMSENCRYPTION_AT_ALL = "SC-NO-CMSENCRYPTION-AT-ALL";

    private boolean withCmsEncryption = checkCmsEnccryptionToUse();

    @Inject
    public SwitchableCmsEncryptionImpl(ASNCmsEncryptionConfig encryptionConfig) {
        super(encryptionConfig);
    }

    @Override
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, Set<PublicKeyIDWithPublicKey> publicKeys) {
        if (withCmsEncryption) {
            return super.buildEncryptionOutputStream(dataContentStream, publicKeys);
        }
        return dataContentStream;
    }

    @Override
    public OutputStream buildEncryptionOutputStream(OutputStream dataContentStream, SecretKey secretKey, KeyID secretKeyID) {
        if (withCmsEncryption) {
            return super.buildEncryptionOutputStream(dataContentStream, secretKey, secretKeyID);
        }
        return dataContentStream;
    }

    @Override
    public InputStream buildDecryptionInputStream(InputStream inputStream, Function<Set<String>, Map<String, Key>> keysByIds) {
        if (withCmsEncryption) {
            return super.buildDecryptionInputStream(inputStream, keysByIds);
        }
        return inputStream;
    }

    public static boolean checkCmsEnccryptionToUse() {
        String value = System.getProperty(NO_CMSENCRYPTION_AT_ALL);
        if (value != null) {
            if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                log.debug("cms encryption is on");
                return true;
            }
            if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                log.debug("cms encryption is off");
                return false;
            }
            throw new RuntimeException("value " + value + " for " + NO_CMSENCRYPTION_AT_ALL + " is unknown");
        }
        log.debug("cms encryption is on");
        return true;
    }
}
