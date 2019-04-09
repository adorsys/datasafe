package de.adorsys.docusafe2.business.impl.dfs;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;

import javax.inject.Inject;

public class DFSConnectionServiceImpl implements DFSConnectionService {

    @Inject
    public DFSConnectionServiceImpl() {
    }

    @Override
    public ExtendedStoreConnection obtain(DFSAccess connectTo) {
        return null;
    }
}
