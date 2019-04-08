package de.adorsys.docusafe2.business.api.types.profile;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserPublicProfile {

    @NonNull
    private final BucketPath publicKeys;

    @NonNull
    private final BucketPath inbox;
}
