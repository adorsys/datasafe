package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

public interface EncryptedResourceResolver {

    PrivateResource encrypt(UserIDAuth auth, PrivateResource resource);
    AbsoluteLocation<PrivateResource> encryptAndResolvePath(UserIDAuth auth, PrivateResource resource);
    AbsoluteLocation<PrivateResource> decryptAndResolvePath(
            UserIDAuth auth, PrivateResource resource, PrivateResource root
    );
}
