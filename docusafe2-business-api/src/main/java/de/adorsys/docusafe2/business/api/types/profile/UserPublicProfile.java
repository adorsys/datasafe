package de.adorsys.docusafe2.business.api.types.profile;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Data;

@Data
public class UserPublicProfile {

    private final BucketPath publicKeys;
    private final BucketPath inbox;
}
