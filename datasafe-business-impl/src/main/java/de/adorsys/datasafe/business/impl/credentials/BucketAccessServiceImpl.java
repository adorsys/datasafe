package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPublicResource;

import javax.inject.Inject;

/**
 * Specifies how to access desired user resource (i.e. private bucket).
 */
public class BucketAccessServiceImpl implements BucketAccessService {

    @Inject
    public BucketAccessServiceImpl() {
    }

    @Override
    public PrivateResource privateAccessFor(UserIDAuth user, ResourceLocation bucket) {
        return new DefaultPrivateResource(
                bucket.locationWithAccess()
        );
    }

    @Override
    public PublicResource publicAccessFor(UserID user, ResourceLocation bucket) {
        return new DefaultPublicResource(
                bucket.locationWithAccess()
        );
    }
}
