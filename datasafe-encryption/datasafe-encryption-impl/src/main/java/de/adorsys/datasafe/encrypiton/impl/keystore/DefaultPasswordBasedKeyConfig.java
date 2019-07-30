package de.adorsys.datasafe.encrypiton.impl.keystore;

import de.adorsys.datasafe.encrypiton.impl.keystore.types.PasswordBasedKeyConfig;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;

import javax.inject.Inject;

@RuntimeDelegate
public class DefaultPasswordBasedKeyConfig implements PasswordBasedKeyConfig {

    @Inject
    public DefaultPasswordBasedKeyConfig() {
    }

    @Override
    public String secretKeyFactoryId() {
        return "PBEWithHmacSHA256AndAES_256";
    }
}
