package de.adorsys.docusafe2.business.impl.document;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.access.DocumentReadService;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.ReadRequest;

import javax.inject.Inject;

public class CMSDocumentReadService implements DocumentReadService {

    private final DFSConnectionService dfs;

    @Inject
    public CMSDocumentReadService(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    @Override
    public void read(ReadRequest request) {
        ExtendedStoreConnection connection = dfs.obtain(request.getFrom());
        connection.getBlob(request.getFrom().getPath());
    }
}
