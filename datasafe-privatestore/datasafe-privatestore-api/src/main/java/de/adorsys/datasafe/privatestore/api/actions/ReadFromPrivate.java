package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import java.io.InputStream;

public interface ReadFromPrivate {

    InputStream read(ReadRequest<UserIDAuth, PrivateResource> request);
}
