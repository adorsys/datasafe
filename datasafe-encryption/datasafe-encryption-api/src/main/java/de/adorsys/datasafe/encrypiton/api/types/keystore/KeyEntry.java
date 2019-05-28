package de.adorsys.datasafe.encrypiton.api.types.keystore;

public interface KeyEntry {
    ReadKeyPassword getReadKeyPassword();

    String getAlias();
}
