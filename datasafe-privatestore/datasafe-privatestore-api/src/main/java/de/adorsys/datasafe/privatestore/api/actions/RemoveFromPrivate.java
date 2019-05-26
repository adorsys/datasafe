package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

public interface RemoveFromPrivate {

    void remove(RemoveRequest<UserIDAuth, PrivateResource> request);
}
