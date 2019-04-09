package de.adorsys.docusafe2.business.impl.credentials;

import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.keystore.PublicKeyService;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;

@Module
public class DefaultCredentialsModule {

    @Provides
    public BucketAccessService bucketAccessService(
            UserProfileService profileService, DFSCredentialsService dfsCredentialsService) {
        return new BucketAccessServiceImpl(profileService, dfsCredentialsService);
    }

    @Provides
    public PublicKeyService publicKeyService(BucketAccessService bucketAccessService) {
        return new PublicKeyServiceImpl(bucketAccessService);
    }

    @Provides
    public PrivateKeyService privateKeyService(BucketAccessService bucketAccessService) {
        return new PrivateKeyServiceImpl(bucketAccessService);
    }

    @Provides
    public DFSCredentialsService dfsCredentialsService() {
        return new DFSCredentialsServiceImpl();
    }
}
