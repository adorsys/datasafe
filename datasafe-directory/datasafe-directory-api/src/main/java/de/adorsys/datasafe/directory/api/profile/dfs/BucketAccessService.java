package de.adorsys.datasafe.directory.api.profile.dfs;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;

public interface BucketAccessService {

    AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, ResourceLocation bucket);
    AbsoluteLocation<PublicResource> publicAccessFor(UserID user, ResourceLocation bucket);
}
