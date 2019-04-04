package de.adorsys.docusafe2.keystore.api.types;

import javax.security.auth.callback.CallbackHandler;

public interface KeyEntry {
    CallbackHandler getPasswordSource();

    String getAlias();
}
