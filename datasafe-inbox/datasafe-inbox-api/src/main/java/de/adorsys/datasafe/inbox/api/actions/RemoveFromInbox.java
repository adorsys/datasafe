package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

public interface RemoveFromInbox {

    void remove(RemoveRequest<UserIDAuth, PrivateResource> request);
}
