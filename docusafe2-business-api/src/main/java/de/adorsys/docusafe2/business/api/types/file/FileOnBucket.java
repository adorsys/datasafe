package de.adorsys.docusafe2.business.api.types.file;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class FileOnBucket {

    @NonNull
    private final BucketPath path;

    private final boolean isDir;
}
