package de.adorsys.datasafe.storage.api.actions;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

public interface StorageCheckService {

    boolean objectExists(AbsoluteLocation location);
}
