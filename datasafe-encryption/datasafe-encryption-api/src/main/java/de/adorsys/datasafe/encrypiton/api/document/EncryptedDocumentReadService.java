package de.adorsys.datasafe.encrypiton.api.document;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import java.io.InputStream;

/**
 * File read operation at a given location.
 */
public interface EncryptedDocumentReadService {

    InputStream read(ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> location);
}
