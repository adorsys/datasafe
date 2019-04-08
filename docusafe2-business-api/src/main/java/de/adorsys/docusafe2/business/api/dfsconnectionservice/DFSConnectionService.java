package de.adorsys.docusafe2.business.api.dfsconnectionservice;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.types.DocumentContent;

import java.util.List;

public interface DFSConnectionService {

    public void putBlob (DocumentContent documentContent, BucketPath bucketPath);
    public DocumentContent getBlob (BucketPath bucketPath);
    public boolean blobExists (BucketPath bucketPath);
    public void removeBlob (BucketPath bucketPath);
    public void removeBlobFolder (BucketDirectory bucketDirectory);
    public List<BucketDirectory> list(BucketDirectory bucketDirectory, boolean listRecursiveFlag);
    public List<BucketDirectory> listAllDirectories();

}
