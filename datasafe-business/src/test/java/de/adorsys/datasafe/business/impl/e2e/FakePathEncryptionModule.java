package de.adorsys.datasafe.business.impl.e2e;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionConfig;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption.SymmetricPathEncryptionServiceImpl;

import javax.inject.Singleton;

import static org.mockito.Mockito.spy;

@Module
public abstract class FakePathEncryptionModule {

    @Binds
    abstract PathEncryptionConfig config(DefaultPathEncryption config);

    @Provides
    @Singleton
    static PathEncryption pathEncryption(PathEncryptionImpl impl) {
        return spy(impl);
    }

    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}