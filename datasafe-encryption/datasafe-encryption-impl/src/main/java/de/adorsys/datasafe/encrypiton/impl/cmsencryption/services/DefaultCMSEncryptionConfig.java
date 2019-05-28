package de.adorsys.datasafe.business.impl.cmsencryption.services;

import de.adorsys.datasafe.business.api.types.CMSEncryptionConfig;
import de.adorsys.datasafe.business.api.types.DatasafeCryptoAlgorithm;

public class DefaultCMSEncryptionConfig extends CMSEncryptionConfig {

    public DefaultCMSEncryptionConfig() {
        super(DatasafeCryptoAlgorithm.AES256_CBC);
    }
}
