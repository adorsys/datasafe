package de.adorsys.datasafe.business.impl.document;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.domain.StorageMetadata;
import de.adorsys.dfs.connection.api.domain.StorageType;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;

import javax.inject.Inject;

public class StorageMetadataMapper {

    @Inject
    public StorageMetadataMapper() {
    }

    public FileOnBucket map(BucketDirectory root, StorageMetadata metadata) {
        return FileOnBucket.builder()
                .path(root.appendName(metadata.getName()))
                .isDir(metadata.getType().equals(StorageType.BLOB))
                .build();
    }
}
