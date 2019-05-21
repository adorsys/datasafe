package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.action.VersionStrategy;

/**
 * Versioning strategy - i.e. read only latest.
 * @param <V> Versioning class.
 */
public interface WithVersionStrategy<V extends VersionStrategy> {

    V getStrategy();
}
