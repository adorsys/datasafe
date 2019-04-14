package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPublicProfile {

    @NonNull
    private final BucketPath publicKeys;

    @NonNull
    private final BucketPath inbox;
}
