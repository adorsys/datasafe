package de.adorsys.datasafe.business.impl.document.cms;

import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentReadService;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Read CMS-encrypted document from DFS.
 */
public class CMSDocumentReadService implements DocumentReadService {

    private final DFSConnectionService dfs;
    private final CMSEncryptionService cms;

    @Inject
    public CMSDocumentReadService(DFSConnectionService dfs, CMSEncryptionService cms) {
        this.dfs = dfs;
        this.cms = cms;
    }

    @Override
    @SneakyThrows
    public InputStream read(ReadRequest request) {
        DFSConnection connection = dfs.obtain(request.getFrom());
        // FIXME DFS connection streaming https://github.com/adorsys/docusafe2/issues/5
        Payload payload = connection.getBlob(new BucketPath(request.getFrom().getPhysicalPath().toString()));

        return cms.buildDecryptionInputStream(
                new ByteArrayInputStream(payload.getData()),
                request.getKeyStore()
        );
    }
}
