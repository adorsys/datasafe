package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

/**
 * Removes users' file from INBOX.
 */
public interface RemoveFromInbox {

    /**
     * Deletes users' file inside users' INBOX
     * @param request Resource location (relative or absolute)
     */
    void remove(RemoveRequest<UserIDAuth, PrivateResource> request);
}
