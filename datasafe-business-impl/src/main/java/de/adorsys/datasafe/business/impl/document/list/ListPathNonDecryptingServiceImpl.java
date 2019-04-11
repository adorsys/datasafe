package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.service.api.ExtendedStoreConnection;
import de.adorsys.datasafe.business.api.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.ListRequest;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;
import de.adorsys.datasafe.business.impl.document.StorageMetadataMapper;

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
        ExtendedStoreConnection connection = dfs.obtain(request.getLocation());

        BucketDirectory bucketPath = request.getLocation().getPhysicalPath().getBucketDirectory();

        // TODO - {@link ExtendedStoreConnection} should return stream instead of list
        return connection
                .list(bucketPath, request.getRecursiveFlag())
                .stream()
                .map(it -> mapper.map(bucketPath, it));
    }
}
