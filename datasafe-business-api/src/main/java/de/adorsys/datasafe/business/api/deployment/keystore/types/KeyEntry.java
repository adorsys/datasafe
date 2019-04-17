package de.adorsys.datasafe.business.api.deployment.keystore.types;

import javax.security.auth.callback.CallbackHandler;

public interface KeyEntry {
    CallbackHandler getPasswordSource();

    String getAlias();
}
