package de.adorsys.datasafe.business.api.types.profile;

import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CreateUserPrivateProfile implements PrivateProfile<DFSAccess> {

    @NonNull
    private final UserIDAuth id;

    @NonNull
    private final DFSAccess keystore;

    @NonNull
    private final DFSAccess privateStorage;
}
