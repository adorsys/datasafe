package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.PasswordClearingOutputStream;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

/**
 * Writes to file in user privatespace.
 */
public interface WriteToPrivate {

    /**
     * Encrypts stream content and writes it into resource specified by {@code request} within privatespace
     * @param request Where to write stream (location can be relative/absolute)
     * @return Stream that will get encrypted and stored within user privatespace.
     * @apiNote Returned stream should be closed properly
     */
    PasswordClearingOutputStream write(WriteRequest<UserIDAuth, PrivateResource> request);
}
