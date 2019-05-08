package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserPrivateProfile {

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> keystore;

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> privateStorage;

    @NonNull
    private final AbsoluteResourceLocation<PrivateResource> inboxWithFullAccess;

    @Override
    public String toString() {
        return "UserPrivateProfile{" +
                "keystore=" + keystore.location().getPath() +
                ", privateStorage=" + privateStorage.location().getPath() +
                ", inboxWithFullAccess=" + inboxWithFullAccess.location().getPath() +
                '}';
    }
}
