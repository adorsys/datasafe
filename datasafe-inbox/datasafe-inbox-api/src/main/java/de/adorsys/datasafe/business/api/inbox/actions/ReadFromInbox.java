package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.io.InputStream;

public interface ReadFromInbox {

    InputStream read(ReadRequest<UserIDAuth, PrivateResource> request);
}
