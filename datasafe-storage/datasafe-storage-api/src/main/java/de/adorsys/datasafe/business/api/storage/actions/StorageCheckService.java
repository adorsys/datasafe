package de.adorsys.datasafe.business.api.storage.actions;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;

public interface StorageCheckService {

    boolean objectExists(AbsoluteResourceLocation location);
}
