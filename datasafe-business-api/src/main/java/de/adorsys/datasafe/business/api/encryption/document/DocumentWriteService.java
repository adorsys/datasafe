package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.io.OutputStream;

/**
 * Raw file write operation at a given location.
 */
public interface DocumentWriteService {

    OutputStream write(PrivateResource location);
}


