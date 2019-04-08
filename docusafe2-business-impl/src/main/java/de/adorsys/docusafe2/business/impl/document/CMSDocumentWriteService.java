package de.adorsys.docusafe2.business.impl.document;

import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.document.DocumentWriteService;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.WriteRequest;

import javax.inject.Inject;

public class CMSDocumentWriteService implements DocumentWriteService {

    private final DFSConnectionService dfs;

    @Inject
    public CMSDocumentWriteService(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    @Override
    public void write(WriteRequest request) {
        ExtendedStoreConnection connection = dfs.obtain(request.getTo());
    }
}
