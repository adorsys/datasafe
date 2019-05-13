package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;

import java.io.OutputStream;

public interface WriteToPrivate {

    OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request);
}
