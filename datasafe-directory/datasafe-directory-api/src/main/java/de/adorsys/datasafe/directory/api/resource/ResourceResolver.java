package de.adorsys.datasafe.directory.api.resource;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;

public interface ResourceResolver {

    AbsoluteLocation<PublicResource> resolveRelativeToPublicInbox(UserID userID, PublicResource resource);

    AbsoluteLocation<PrivateResource> resolveRelativeToPrivateInbox(UserIDAuth userID, PrivateResource resource);

    AbsoluteLocation<PrivateResource> resolveRelativeToPrivate(UserIDAuth userID, PrivateResource resource);

    <T extends ResourceLocation<T>> boolean isAbsolute(T resource);
}
