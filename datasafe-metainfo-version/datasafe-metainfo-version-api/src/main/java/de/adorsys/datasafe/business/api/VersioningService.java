package de.adorsys.datasafe.business.api;

import de.adorsys.datasafe.business.api.types.action.Version;

public interface VersioningService<V extends Version> {

    VersionedPrivateSpaceService<V> privateSpace(V version);
}
