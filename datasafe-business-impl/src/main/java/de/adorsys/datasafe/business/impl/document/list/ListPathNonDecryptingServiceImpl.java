package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.datasafe.business.api.deployment.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.net.URI;
import java.util.stream.Stream;

public class ListPathNonDecryptingServiceImpl implements DocumentListService {

    private final DFSConnectionService dfs;

    @Inject
    public ListPathNonDecryptingServiceImpl(DFSConnectionService dfs) {
        this.dfs = dfs;
    }

    @Override
    public Stream<URI> list(ListRequest request) {
        DFSConnection connection = dfs.obtain(request.getLocation());

        return connection
            .list(
                new BucketDirectory(request.getLocation().getPhysicalPath().toString()),
                ListRecursiveFlag.FALSE
            ).stream()
                .map(this::fromBucketPath);
    }

    @SneakyThrows
    private URI fromBucketPath(BucketPath path) {
        return new URI(path.getObjectHandle().getContainer() + "//" + path.getObjectHandle().getName());
    }
}
