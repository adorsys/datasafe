package de.adorsys.datasafe.business.impl.credentials;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.credentials.DFSCredentialsService;
import de.adorsys.datasafe.business.api.deployment.credentials.dto.DFSCredentials;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;

import javax.inject.Inject;
import java.net.URI;
import java.util.function.Function;

/**
 * Specifies how to access desired user resource (i.e. private bucket).
 */
public class BucketAccessServiceImpl implements BucketAccessService {

    private final ProfileRetrievalService profiles;
    private final DFSCredentialsService credentials;

    @Inject
    public BucketAccessServiceImpl(ProfileRetrievalService profiles, DFSCredentialsService credentials) {
        this.profiles = profiles;
        this.credentials = credentials;
    }

    @Override
    public DFSAccess privateAccessFor(UserIDAuth user, Function<ProfileRetrievalService, URI> bucket) {
        URI path = bucket.apply(profiles);

        DFSCredentials creds = credentials.privateUserCredentials(user, path);

        return DFSAccess.builder()
                .physicalPath(path)
                .credentials(creds)
                .build();
    }

    @Override
    public DFSAccess publicAccessFor(UserID user, Function<ProfileRetrievalService, URI> bucket) {
        URI path = bucket.apply(profiles);

        DFSCredentials creds = credentials.publicUserCredentials(user, path);

        return DFSAccess.builder()
                .physicalPath(path)
                .credentials(creds)
                .build();
    }
}
