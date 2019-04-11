package de.adorsys.docusafe2.business.impl.dfs;

import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;

import javax.inject.Inject;

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
