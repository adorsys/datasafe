package de.adorsys.docusafe2.business.impl.document;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.impl.document.dto.WriteRequest;

import javax.inject.Inject;

public class DocumentWriteService {

    private final DFSConnectionService dfs;

    @Inject
    public DocumentWriteService(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    public void write(WriteRequest request) {
        ExtendedStoreConnection connection = dfs.obtain(request.getTo());
    }
}
