package de.adorsys.datasafe.business.api;

import de.adorsys.datasafe.business.api.types.action.Version;
import de.adorsys.datasafe.business.impl.privatespace.PrivateSpaceService;

public interface VersionedPrivateSpaceService<V extends Version> extends PrivateSpaceService {

    V getVersion();
}
