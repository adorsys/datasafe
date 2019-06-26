package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Extracts database URI from resource location.
 */
@FunctionalInterface
public interface DbUriExtractor {

    String extract(AbsoluteLocation location);
}
