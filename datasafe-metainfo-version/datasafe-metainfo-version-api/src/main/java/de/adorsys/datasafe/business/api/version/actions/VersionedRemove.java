package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.actions.RemoveFromPrivate;

public interface VersionedRemove<V extends VersionStrategy> extends RemoveFromPrivate, WithVersionStrategy<V> {
}
