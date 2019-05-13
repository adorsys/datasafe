package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import java.io.InputStream;

public interface ReadFromInbox {

    InputStream read(ReadRequest<UserIDAuth, PrivateResource> request);
}
