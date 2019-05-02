package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.io.OutputStream;

/**
 * File write operation at a given location.
 */
public interface EncryptedDocumentWriteService {

    OutputStream write(WriteRequest<UserID, ResourceLocation> location);
}


