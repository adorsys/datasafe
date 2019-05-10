package de.adorsys.datasafe.business.impl.profile.dfs;

import de.adorsys.datasafe.business.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.*;
import de.adorsys.datasafe.business.api.types.utils.Log;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Specifies how to access desired user resource (i.e. private bucket).
 */

@Slf4j
public class BucketAccessServiceImpl implements BucketAccessService {

    @Inject
    public BucketAccessServiceImpl() {
    }

    @Override
    public AbsoluteResourceLocation<PrivateResource> privateAccessFor(UserIDAuth user, ResourceLocation bucket) {
        log.debug("get private access for user {} and bucket {}", Log.secure(user.getUserID()),
                Log.secure(bucket.location()));
        return new AbsoluteResourceLocation<>(new DefaultPrivateResource(bucket.location()));
    }

    @Override
    public AbsoluteResourceLocation<PublicResource> publicAccessFor(UserID user, ResourceLocation bucket) {
        log.debug("get public access for user {} and bucket {}", Log.secure(user),
                Log.secure(bucket.location()));
        return new AbsoluteResourceLocation<>(new DefaultPublicResource(bucket.location()));
    }
}
