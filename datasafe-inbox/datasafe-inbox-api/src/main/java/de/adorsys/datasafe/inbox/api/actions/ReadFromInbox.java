package de.adorsys.datasafe.inbox.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import java.io.InputStream;

public interface ReadFromInbox {

    InputStream read(ReadRequest<UserIDAuth, PrivateResource> request);
}
