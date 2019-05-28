package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import java.io.OutputStream;

/**
 * Shares file with the user by writing into his INBOX folder.
 */
public interface WriteToInbox {

    /**
     * Encrypts stream content using asymmetric cryptography and writes it into resource specified by {@code request}
     * within INBOX
     * @param request Where to write stream (location can be relative/absolute)
     * @return Stream that will get encrypted and stored within user INBOX.
     * @apiNote Returned stream should be closed properly
     */
    OutputStream write(WriteRequest<UserID, PublicResource> request);
}
