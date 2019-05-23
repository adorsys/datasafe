package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.actions.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.api.privatespace.actions.RemoveFromPrivate;

/**
 * Removes latest resource version (so resource will be invisible in latest view, but its versions are kept)
 * @param <V> Versioning class.
 */
public interface VersionedRemove<V extends VersionStrategy> extends RemoveFromPrivate, WithVersionStrategy<V> {
}
