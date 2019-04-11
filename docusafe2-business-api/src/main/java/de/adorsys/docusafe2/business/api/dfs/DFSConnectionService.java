package de.adorsys.docusafe2.business.api.dfs;

import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.docusafe2.business.api.types.DFSAccess;

public interface DFSConnectionService {

    DFSConnection obtain(DFSAccess connectTo);
}
