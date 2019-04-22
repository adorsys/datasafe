package de.adorsys.datasafe.business.api.storage.document;

import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.io.OutputStream;

/**
 * Raw file write operation at a given location.
 */
public interface StorageWriteService {

    OutputStream write(ResourceLocation location);
}


