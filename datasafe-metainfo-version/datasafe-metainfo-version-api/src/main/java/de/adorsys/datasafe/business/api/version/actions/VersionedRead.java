package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.actions.ReadFromPrivate;

/**
 * Reads latest resource version.
 * @param <V> Versioning class.
 */
public interface VersionedRead<V extends VersionStrategy> extends ReadFromPrivate, WithVersionStrategy<V> {
}
