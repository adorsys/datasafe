package de.adorsys.docusafe2.business.impl.document;

import com.google.common.io.ByteSource;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.docusafe2.business.api.document.DocumentReadService;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.DocumentContent;
import de.adorsys.docusafe2.business.api.types.ReadRequest;
import lombok.SneakyThrows;
import org.bouncycastle.cms.CMSEnvelopedData;

import javax.inject.Inject;

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
    public void read(ReadRequest request) {
        ExtendedStoreConnection connection = dfs.obtain(request.getFrom());
        Payload payload = connection.getBlob(request.getFrom().getPath());

        // FIXME https://github.com/adorsys/docusafe2/issues/5
        DocumentContent content = cms.decrypt(
                new CMSEnvelopedData(payload.getData()),
                request.getKeyStore()
        );

        ByteSource.wrap(content.getValue()).copyTo(request.getResponse().getData());
    }
}
