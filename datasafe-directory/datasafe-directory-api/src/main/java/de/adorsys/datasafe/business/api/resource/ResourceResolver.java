package de.adorsys.datasafe.business.api.resource;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

public interface ResourceResolver {

    AbsoluteResourceLocation<PublicResource> resolveRelativeToPublicInbox(UserID userID, PublicResource resource);

    AbsoluteResourceLocation<PrivateResource> resolveRelativeToPrivateInbox(UserIDAuth userID, PrivateResource resource);

    AbsoluteResourceLocation<PrivateResource> resolveRelativeToPrivate(UserIDAuth userID, PrivateResource resource);

    <T extends ResourceLocation<T>> boolean isAbsolute(T resource);
}
