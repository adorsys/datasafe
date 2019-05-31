package de.adorsys.datasafe.metainfo.version.api.actions;

import de.adorsys.datasafe.metainfo.version.api.version.WithVersionStrategy;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.types.api.actions.VersionStrategy;

/**
 * Writes resource and marks as latest.
 * @param <V> Versioning class.
 */
public interface VersionedWrite<V extends VersionStrategy> extends WriteToPrivate, WithVersionStrategy<V> {
}
