package de.adorsys.datasafe.business.api.storage.actions;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;

import java.util.stream.Stream;

/**
 * Raw list operation at a given location.
 */
@FunctionalInterface
public interface StorageListService {

    /**
     * Lists bucket contents.
     * @param location absolute bucket path, with credentials, where to list data
     * @return stream of available absolute bucket paths
     */
    Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location);
}
