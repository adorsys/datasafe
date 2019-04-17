package de.adorsys.datasafe.business.impl.dfs;

import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.dfs.connection.impl.factory.DFSConnectionFactory;

import javax.inject.Inject;

// DEPLOYMENT
public class DFSConnectionServiceImpl implements DFSConnectionService {

    private static final DFSConnection CONN = DFSConnectionFactory.get();

    @Inject
    public DFSConnectionServiceImpl() {
    }

    @Override
    public DFSConnection obtain(DFSAccess connectTo) {
        return CONN;
    }
}
