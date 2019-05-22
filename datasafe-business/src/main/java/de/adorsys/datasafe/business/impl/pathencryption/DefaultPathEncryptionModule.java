package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.DefaultPathDigestConfig;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionConfig;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption.SymmetricPathEncryptionServiceImpl;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @CoberturaIgnore
    @Provides
    static DefaultPathDigestConfig digestConfig() {
        return new DefaultPathDigestConfig();
    }

    @CoberturaIgnore
    @Binds
    abstract PathEncryptionConfig config(DefaultPathEncryption config);

    @CoberturaIgnore
    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImpl impl);

    @CoberturaIgnore
    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}
