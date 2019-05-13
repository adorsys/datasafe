package de.adorsys.datasafe.business.api.version.types.keystore;

import javax.security.auth.callback.CallbackHandler;

public interface KeyEntry {
    CallbackHandler getPasswordSource();

    String getAlias();
}
