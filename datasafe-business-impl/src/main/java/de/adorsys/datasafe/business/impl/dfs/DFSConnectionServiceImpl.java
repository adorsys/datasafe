package de.adorsys.datasafe.business.impl.dfs;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.datasafe.business.api.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.types.DFSAccess;

import javax.inject.Inject;

public class DFSConnectionServiceImpl implements DFSConnectionService {

    @Inject
    public DFSConnectionServiceImpl() {
    }

    @Override
    public ExtendedStoreConnection obtain(DFSAccess connectTo) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
