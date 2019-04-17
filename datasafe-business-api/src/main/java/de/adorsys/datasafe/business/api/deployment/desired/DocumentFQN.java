package de.adorsys.datasafe.business.api.deployment.desired;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;

public class DocumentFQN extends BucketPath {

    public DocumentFQN(String path) {
        super(path);
    }

    public DocumentFQN(BucketPath bucketPath) {
        super(bucketPath);
    }
}
