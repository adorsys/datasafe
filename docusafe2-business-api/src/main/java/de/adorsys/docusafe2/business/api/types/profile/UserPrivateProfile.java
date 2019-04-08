package de.adorsys.docusafe2.business.api.types.profile;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserPrivateProfile {

    @NonNull
    private final BucketPath keystore;

    @NonNull
    private final BucketPath privateStorage;
}
