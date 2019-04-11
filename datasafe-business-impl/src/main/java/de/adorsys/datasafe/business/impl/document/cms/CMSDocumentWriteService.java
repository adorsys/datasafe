package de.adorsys.datasafe.business.impl.document.cms;

import com.google.common.io.ByteStreams;
import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.dfs.connection.api.service.impl.SimplePayloadImpl;
import de.adorsys.datasafe.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.document.DocumentWriteService;
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
        ExtendedStoreConnection connection = dfs.obtain(request.getTo());

        // FIXME https://github.com/adorsys/datasafe/issues/5
        CMSEnvelopedData data = cms.encrypt(
                new DocumentContent(ByteStreams.toByteArray(request.getData().getData())),
                request.getKeyWithId().getPublicKey(),
                request.getKeyWithId().getPublicKeyId()
        );

        connection.putBlob(request.getTo().getPath(), new SimplePayloadImpl(data.getEncoded()));
    }
}
