package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.WriteInboxRequest;
import de.adorsys.datasafe.types.api.resource.PublicResource;

import java.io.OutputStream;
import java.util.Set;

/**
 * Shares file with the user by writing into his INBOX folder.
 */
public interface WriteToInbox {

    /**
     * Shares data with multiple users specified by their UserID's and encrypts it in a way that only recipients
     * can read encrypted document.
     * @param request Where to write stream (location can be relative/absolute)
     * @return Stream that will get encrypted and stored within user INBOX.
     * @apiNote Returned stream should be closed properly
     */
    OutputStream write(WriteInboxRequest<UserIDAuth, Set<UserID>, PublicResource> request);
}
