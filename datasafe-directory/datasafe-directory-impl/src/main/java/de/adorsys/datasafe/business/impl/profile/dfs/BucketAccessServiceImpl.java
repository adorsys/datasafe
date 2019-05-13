package de.adorsys.datasafe.business.impl.profile.dfs;

import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.version.types.UserID;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.resource.*;

import javax.inject.Inject;

/**
 * Specifies how to access desired user resource (i.e. private bucket).
 */
public class BucketAccessServiceImpl implements BucketAccessService {

    @Inject
    public BucketAccessServiceImpl() {
    }

    @Override
    public AbsoluteResourceLocation<PrivateResource> privateAccessFor(UserIDAuth user, ResourceLocation bucket) {
        return new AbsoluteResourceLocation<>(new DefaultPrivateResource(bucket.location()));
    }

    @Override
    public AbsoluteResourceLocation<PublicResource> publicAccessFor(UserID user, ResourceLocation bucket) {
        return new AbsoluteResourceLocation<>(new DefaultPublicResource(bucket.location()));
    }
}
