package de.adorsys.datasafe.business.impl.document.cms;

import com.google.common.io.ByteStreams;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.service.impl.SimplePayloadImpl;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.types.DocumentContent;
import de.adorsys.datasafe.business.api.types.WriteRequest;
import lombok.SneakyThrows;
import org.bouncycastle.cms.CMSEnvelopedData;

import javax.inject.Inject;

/**
 * Write CMS-encrypted document to DFS.
 */
public class CMSDocumentWriteService implements DocumentWriteService {

    private final DFSConnectionService dfs;
    private final CMSEncryptionService cms;

    @Inject
    public CMSDocumentWriteService(DFSConnectionService dfs, CMSEncryptionService cms) {
        this.dfs = dfs;
        this.cms = cms;
    }

    @Override
    @SneakyThrows
    public void write(WriteRequest request) {
        DFSConnection connection = dfs.obtain(request.getTo());

        // FIXME https://github.com/adorsys/docusafe2/issues/5
        CMSEnvelopedData data = cms.encrypt(
                new DocumentContent(ByteStreams.toByteArray(request.getData().getData())),
                request.getKeyWithId().getPublicKey(),
                request.getKeyWithId().getKeyID()
        );

        connection.putBlob(request.getTo().getPhysicalPath(), new SimplePayloadImpl(data.getEncoded()));
    }
}
