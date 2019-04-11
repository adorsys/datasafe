package de.adorsys.docusafe2.business.impl.document.list;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.types.ListRequest;
import de.adorsys.docusafe2.business.api.types.file.FileOnBucket;
import de.adorsys.docusafe2.business.impl.document.StorageMetadataMapper;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListPathNonDecryptingServiceImpl implements DocumentListService {

    private final DFSConnectionService dfs;
    private final StorageMetadataMapper mapper;

    @Inject
    public ListPathNonDecryptingServiceImpl(DFSConnectionService dfs, StorageMetadataMapper mapper) {
        this.dfs = dfs;
        this.mapper = mapper;
    }

    @Override
    public Stream<FileOnBucket> list(ListRequest request) {
        DFSConnection connection = dfs.obtain(request.getLocation());

        BucketDirectory bucketPath = request.getLocation().getPhysicalPath().getBucketDirectory();

        // TODO - {@link ExtendedStoreConnection} should return stream instead of list
        /*return connection
                .list(bucketPath, request.getRecursiveFlag())
                .stream()
                .map(it -> mapper.map(bucketPath, it));*/
        return null;
    }
}
