package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.action.VersionStrategy;

public interface WithVersionStrategy<V extends VersionStrategy> {

    V getStrategy();
}
