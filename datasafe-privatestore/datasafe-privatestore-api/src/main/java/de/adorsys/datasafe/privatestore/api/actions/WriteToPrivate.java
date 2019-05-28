package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import java.io.OutputStream;

public interface WriteToPrivate {

    OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request);
}
