package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.util.stream.Stream;

/**
 * List operation at a given location.
 */
public interface DocumentListService {

    /**
     * Lists bucket contents.
     * @param location bucket descriptor, with credentials, where to list data
     * @return stream of available bucket paths
     */
    Stream<PrivateResource> list(PrivateResource location);
}
