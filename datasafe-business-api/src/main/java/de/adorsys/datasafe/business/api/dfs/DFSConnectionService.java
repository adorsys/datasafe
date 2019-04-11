package de.adorsys.datasafe.business.api.dfs;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.datasafe.business.api.types.DFSAccess;

public interface DFSConnectionService {

    ExtendedStoreConnection obtain(DFSAccess connectTo);
}
