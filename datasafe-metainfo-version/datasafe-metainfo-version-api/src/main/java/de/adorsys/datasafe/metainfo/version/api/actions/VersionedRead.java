package de.adorsys.datasafe.metainfo.version.api.actions;

import de.adorsys.datasafe.types.api.actions.VersionStrategy;
import de.adorsys.datasafe.metainfo.version.api.version.WithVersionStrategy;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;

/**
 * Reads latest resource version.
 * @param <V> Versioning class.
 */
public interface VersionedRead<V extends VersionStrategy> extends ReadFromPrivate, WithVersionStrategy<V> {
}
