package de.adorsys.datasafe.business.api.profile.dfs;

import de.adorsys.datasafe.business.api.version.types.UserID;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.version.types.resource.ResourceLocation;

public interface BucketAccessService {

    AbsoluteResourceLocation<PrivateResource> privateAccessFor(UserIDAuth user, ResourceLocation bucket);
    AbsoluteResourceLocation<PublicResource> publicAccessFor(UserID user, ResourceLocation bucket);
}
