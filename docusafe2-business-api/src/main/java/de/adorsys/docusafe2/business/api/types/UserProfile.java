package de.adorsys.docusafe2.business.api.types;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Data;

@Data
public class UserProfile {

    private final BucketPath inbox;
    private final BucketPath privateStorage;
}
