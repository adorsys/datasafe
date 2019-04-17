package de.adorsys.datasafe.business.api.types.privatespace;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PrivateReadRequest {

    @NonNull
    private final UserIDAuth owner;

    @NonNull
    private final PrivateBucketPath path;

    @NonNull
    private final FileOut response;
}
