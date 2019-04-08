package de.adorsys.docusafe2.business.impl.dfsconnectionservice;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.dfsconnectionservice.DFSConnectionService;
import de.adorsys.docusafe2.business.api.types.DocumentContent;

import java.util.List;

public class SystemDSFFilesystemConnectionService implements DFSConnectionService {
    @Override
    public void putBlob(DocumentContent documentContent, BucketPath bucketPath) {

    }

    @Override
    public DocumentContent getBlob(BucketPath bucketPath) {
        return null;
    }

    @Override
    public boolean blobExists(BucketPath bucketPath) {
        return false;
    }

    @Override
    public void removeBlob(BucketPath bucketPath) {

    }

    @Override
    public void removeBlobFolder(BucketDirectory bucketDirectory) {

    }

    @Override
    public List<BucketDirectory> list(BucketDirectory bucketDirectory, boolean listRecursiveFlag) {
        return null;
    }

    @Override
    public List<BucketDirectory> listAllDirectories() {
        return null;
    }
}
