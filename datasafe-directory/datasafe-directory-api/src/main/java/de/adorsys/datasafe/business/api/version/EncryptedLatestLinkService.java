package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

public interface EncryptedLatestLinkService {
    AbsoluteLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth auth, PrivateResource resource, UserPrivateProfile privateProfile);

    AbsoluteLocation<PrivateResource> readLinkAndDecrypt(
            UserIDAuth owner,
            AbsoluteLocation<PrivateResource> latestLink,
            UserPrivateProfile privateProfile);
}
