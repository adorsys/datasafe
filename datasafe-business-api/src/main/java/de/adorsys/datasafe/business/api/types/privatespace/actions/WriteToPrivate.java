package de.adorsys.datasafe.business.api.types.privatespace.actions;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.io.OutputStream;

public interface WriteToPrivate {

    OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request);
}
