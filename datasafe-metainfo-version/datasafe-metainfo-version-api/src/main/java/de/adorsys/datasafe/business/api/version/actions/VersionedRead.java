package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.actions.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.api.privatespace.actions.ReadFromPrivate;

/**
 * Reads latest resource version.
 * @param <V> Versioning class.
 */
public interface VersionedRead<V extends VersionStrategy> extends ReadFromPrivate, WithVersionStrategy<V> {
}
