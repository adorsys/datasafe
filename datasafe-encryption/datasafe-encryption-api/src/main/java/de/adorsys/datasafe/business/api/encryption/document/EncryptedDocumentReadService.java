package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.io.InputStream;

/**
 * File read operation at a given location.
 */
public interface EncryptedDocumentReadService {

    InputStream read(ReadRequest<UserIDAuth, AbsoluteResourceLocation<PrivateResource>> location);
}
