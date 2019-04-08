package de.adorsys.docusafe2.business.impl.document;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.impl.document.dto.ReadRequest;

import javax.inject.Inject;

public class DocumentReadService {

    private final DFSConnectionService dfs;

    @Inject
    public DocumentReadService(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    public void read(ReadRequest request) {
        ExtendedStoreConnection connection = dfs.obtain(request.getFrom());
        connection.getBlob(request.getFrom().getPath());
    }
}
