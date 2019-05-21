package de.adorsys.datasafe.business.api.storage.actions;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;

public interface StorageCheckService {

    boolean objectExists(AbsoluteLocation location);
}
