package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.actions.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.api.privatespace.actions.WriteToPrivate;

/**
 * Writes resource and marks as latest.
 * @param <V> Versioning class.
 */
public interface VersionedWrite<V extends VersionStrategy> extends WriteToPrivate, WithVersionStrategy<V> {
}
