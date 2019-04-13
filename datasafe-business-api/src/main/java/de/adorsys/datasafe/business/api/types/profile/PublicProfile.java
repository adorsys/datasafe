package de.adorsys.datasafe.business.api.types.profile;

public interface PublicProfile<T> {

    T getPublicKeys();
    T getInbox();
}
