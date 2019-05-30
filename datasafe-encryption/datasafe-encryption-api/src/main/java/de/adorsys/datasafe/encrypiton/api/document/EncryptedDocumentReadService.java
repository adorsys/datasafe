package de.adorsys.datasafe.encrypiton.api.document;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import java.io.InputStream;

/**
 * Encrypted document read operation.
 */
public interface EncryptedDocumentReadService {

    /**
     * Reads and decrypts encrypted document, handles document encryption type internally.
     * @param location From where to read data
     * @return Decrypted document content stream
     */
    InputStream read(ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> location);
}
