package de.adorsys.datasafe.business.api.storage.document;

import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.io.InputStream;

/**
 * Raw file read operation at a given location.
 */
public interface StorageReadService {

    InputStream read(ResourceLocation location);
}
