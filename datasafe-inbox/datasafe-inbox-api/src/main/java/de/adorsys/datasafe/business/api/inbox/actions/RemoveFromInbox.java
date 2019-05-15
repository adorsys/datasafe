package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

public interface RemoveFromInbox {

    void remove(RemoveRequest<UserIDAuth, PrivateResource> request);
}
