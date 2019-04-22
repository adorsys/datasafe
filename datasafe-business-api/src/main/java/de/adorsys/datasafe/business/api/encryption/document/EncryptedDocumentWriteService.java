package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;

import java.io.OutputStream;

/**
 * File write operation at a given location.
 */
public interface EncryptedDocumentWriteService {

    OutputStream write(WriteRequest<UserID> location);
}


