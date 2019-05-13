package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.version.types.action.VersionStrategy;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;

public interface VersionedPrivateSpaceService<V extends VersionStrategy> extends PrivateSpaceService {

    V getVersionStrategy();
}
