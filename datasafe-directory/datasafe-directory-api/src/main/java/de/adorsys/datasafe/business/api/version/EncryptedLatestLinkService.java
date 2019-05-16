package de.adorsys.datasafe.business.api.version;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.UserPrivateProfile;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

public interface EncryptedLatestLinkService {
    AbsoluteResourceLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth auth, PrivateResource resource, UserPrivateProfile privateProfile);

    AbsoluteResourceLocation<PrivateResource> readLinkAndDecrypt(
            UserIDAuth owner,
            AbsoluteResourceLocation<PrivateResource> latestLink,
            UserPrivateProfile privateProfile);
}
