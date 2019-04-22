package de.adorsys.datasafe.business.api.storage.document;

import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.util.stream.Stream;

/**
 * Raw list operation at a given location.
 */
public interface StorageListService {

    /**
     * Lists bucket contents.
     * @param location bucket descriptor, with credentials, where to list data
     * @return stream of available bucket paths
     */
    Stream<ResourceLocation> list(ResourceLocation location);
}
