package de.adorsys.datasafe.business.api.version.actions;

import de.adorsys.datasafe.business.api.types.action.VersionStrategy;
import de.adorsys.datasafe.business.api.version.WithVersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.actions.WriteToPrivate;

public interface VersionedWrite<V extends VersionStrategy> extends WriteToPrivate, WithVersionStrategy<V> {
}
