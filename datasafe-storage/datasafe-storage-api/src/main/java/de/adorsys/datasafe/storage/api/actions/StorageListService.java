package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;

import java.util.stream.Stream;

/**
 * Raw list operation at a given location. Paths use URL-encoding.
 */
@FunctionalInterface
public interface StorageListService {

    /**
     * Lists bucket contents.
     * @param location absolute bucket path with credentials (if necessary) where to list data
     * @return stream of available absolute bucket paths
     */
    Stream<AbsoluteLocation<ResolvedResource>> list(AbsoluteLocation location);
}
