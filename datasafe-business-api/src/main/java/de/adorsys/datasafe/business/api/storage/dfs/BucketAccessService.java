package de.adorsys.datasafe.business.api.storage.dfs;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

public interface BucketAccessService {

    PrivateResource privateAccessFor(UserIDAuth user, ResourceLocation dataLocation);
    PublicResource publicAccessFor(UserID user, ResourceLocation dataLocation);
}
