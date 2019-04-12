package de.adorsys.datasafe.business.impl.dfs;

import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.types.DFSAccess;

import javax.inject.Inject;

// DEPLOYMENT
public class DFSConnectionServiceImpl implements DFSConnectionService {

    @Inject
    public DFSConnectionServiceImpl() {
    }

    @Override
    public DFSConnection obtain(DFSAccess connectTo) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
