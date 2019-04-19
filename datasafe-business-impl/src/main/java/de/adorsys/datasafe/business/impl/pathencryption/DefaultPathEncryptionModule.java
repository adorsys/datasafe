package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.deployment.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.bucketpathencryption.BucketPathEncryptionService;

/**
 * This module is responsible for providing CMS encryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImpl impl);

    @Binds
    abstract BucketPathEncryptionService bucketPathEncryptionService(BucketPathEncryptionServiceImpl impl);
}
