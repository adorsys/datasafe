package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.io.InputStream;

/**
 * File read operation at a given location.
 */
public interface DocumentReadService {

    InputStream read(PrivateResource location);
}
