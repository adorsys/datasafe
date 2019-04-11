package de.adorsys.datasafe.business.api.keystore.types;

import javax.security.auth.callback.CallbackHandler;

public interface KeyEntry {
    CallbackHandler getPasswordSource();

    String getAlias();
}
