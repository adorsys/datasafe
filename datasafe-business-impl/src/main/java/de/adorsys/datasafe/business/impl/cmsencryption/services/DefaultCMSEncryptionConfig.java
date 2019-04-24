package de.adorsys.datasafe.business.impl.cmsencryption.services;

import de.adorsys.datasafe.business.api.types.CMSEncryptionConfig;
import de.adorsys.datasafe.business.api.types.DatasafeCryptoAlgorithm;

import javax.inject.Inject;

public class DefaultCMSEncryptionConfig extends CMSEncryptionConfig {

    @Inject
    public DefaultCMSEncryptionConfig() {
        super(DatasafeCryptoAlgorithm.AES256_CBC);
    }
}
