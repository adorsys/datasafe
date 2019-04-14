package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;
import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListPathNonDecryptingServiceImpl implements DocumentListService {

    private final DFSConnectionService dfs;

    @Inject
    public ListPathNonDecryptingServiceImpl(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    @Override
    public Stream<FileOnBucket> list(ListRequest request) {
        DFSConnection connection = dfs.obtain(request.getLocation());

        return connection
            .list(
                new BucketDirectory(request.getLocation().getPhysicalPath()),
                ListRecursiveFlag.FALSE
            ).stream()
            .map(it -> FileOnBucket.builder()
                .path(it)
                .isDir(false)
                .build()
            );
    }
}
