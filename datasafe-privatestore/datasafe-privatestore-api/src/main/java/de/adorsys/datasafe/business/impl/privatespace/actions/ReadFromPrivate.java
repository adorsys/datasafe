package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.io.InputStream;

public interface ReadFromPrivate {

    InputStream read(ReadRequest<UserIDAuth, PrivateResource> request);
}
