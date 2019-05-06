package de.adorsys.datasafe.business.impl.testcontainers;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption.SymmetricPathEncryptionServiceImpl;

import javax.inject.Singleton;

import static org.mockito.Mockito.spy;

@Module
public abstract class FakePathEncryptionModule {


    @Provides
    @Singleton
    static PathEncryption pathEncryption(PathEncryptionImpl impl) {
        return spy(impl);
    }

    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}