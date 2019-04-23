package de.adorsys.datasafe.business.impl.testcontainers;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.deployment.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.deployment.keystore.PrivateKeyService;
import de.adorsys.datasafe.business.api.deployment.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.bucketpathencryption.BucketPathEncryptionService;
import de.adorsys.datasafe.business.impl.pathencryption.BucketPathEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.pathencryption.PathEncryptionImpl;

import javax.inject.Singleton;

import static org.mockito.Mockito.spy;

@Module
public abstract class FakePathEncryptionModule {


    @Provides
    @Singleton
    static PathEncryption pathEncryption(BucketPathEncryptionService bs, PrivateKeyService pk, KeyStoreService ks) {
        return spy(new PathEncryptionImpl(bs, pk, ks));
    }

    @Binds
    abstract BucketPathEncryptionService bucketPathEncryptionService(BucketPathEncryptionServiceImpl impl);
}
