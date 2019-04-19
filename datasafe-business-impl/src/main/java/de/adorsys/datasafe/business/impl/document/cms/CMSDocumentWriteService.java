package de.adorsys.datasafe.business.impl.document.cms;

import com.google.common.collect.ImmutableList;
import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentWriteService;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.service.impl.SimplePayloadImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

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
    public OutputStream write(WriteRequest request) {
        DFSConnection connection = dfs.obtain(request.getTo());

        // FIXME Streaming DFS https://github.com/adorsys/docusafe2/issues/5
        OutputStream dfsSink = new PutBlobOnClose(
                new BucketPath(request.getTo().getPhysicalPath().toString()),
                connection
        );

        OutputStream encryptionSink = cms.buildEncryptionOutputStream(
                dfsSink,
                request.getKeyWithId().getPublicKey(),
                request.getKeyWithId().getKeyID()
        );

        return new CloseCoordinatingStream(encryptionSink, ImmutableList.of(encryptionSink, dfsSink));
    }

    /**
     * This class fixes issue that bouncy castle does not close underlying stream - i.e. DFS stream
     * when wrapping it.
     */
    @RequiredArgsConstructor
    private static final class CloseCoordinatingStream extends OutputStream {

        private final OutputStream streamToWrite;
        private final List<OutputStream> streamsToClose;

        @Override
        public void write(int b) throws IOException {
            streamToWrite.write(b);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            streamToWrite.write(bytes, off, len);
        }

        @Override
        @SneakyThrows
        public void close() {
            super.close();
            streamsToClose.forEach(CloseCoordinatingStream::doClose);
        }


        @SneakyThrows
        private static void doClose(OutputStream stream) {
            stream.close();
        }
    }

    @RequiredArgsConstructor
    // FIXME Streaming DFS https://github.com/adorsys/docusafe2/issues/5
    private static final class PutBlobOnClose extends ByteArrayOutputStream {

        private final BucketPath path;
        private final DFSConnection connection;

        @Override
        public void close() throws IOException {
            super.close();
            connection.putBlob(
                    new BucketPath(path),
                    new SimplePayloadImpl(toByteArray())
            );
        }
    }
}
