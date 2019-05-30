package de.adorsys.datasafe.metainfo.version.api.actions;

import de.adorsys.datasafe.metainfo.version.api.version.WithVersionStrategy;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.types.api.actions.VersionStrategy;

/**
 * Removes latest resource version (so resource will be invisible in latest view, but its versions are kept)
 * @param <V> Versioning class.
 */
public interface VersionedRemove<V extends VersionStrategy> extends RemoveFromPrivate, WithVersionStrategy<V> {
}
