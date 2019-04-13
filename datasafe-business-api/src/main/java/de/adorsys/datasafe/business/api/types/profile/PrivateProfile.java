package de.adorsys.datasafe.business.api.types.profile;

public interface PrivateProfile<T> {

    T getKeystore();
    T getPrivateStorage();
}
