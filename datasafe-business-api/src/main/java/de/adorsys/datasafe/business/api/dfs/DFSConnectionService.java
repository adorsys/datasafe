package de.adorsys.datasafe.business.api.dfs;

import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;

public interface DFSConnectionService {

    DFSConnection obtain(DFSAccess connectTo);
}
