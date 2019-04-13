package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPrivateProfile implements PrivateProfile<BucketPath> {

    @NonNull
    private final BucketPath keystore;

    @NonNull
    private final BucketPath privateStorage;
}
