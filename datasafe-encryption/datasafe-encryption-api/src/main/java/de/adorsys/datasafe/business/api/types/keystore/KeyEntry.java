package de.adorsys.datasafe.business.api.types.keystore;

public interface KeyEntry {
    ReadKeyPassword getReadKeyPassword();

    String getAlias();
}
