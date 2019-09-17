package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathEncryptorDecryptorRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptorDecryptor;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.SymmetricPathEncryptionServiceImplRuntimeDelegatable;
import org.cryptomator.siv.SivMode;

/**
 * This module is responsible for providing pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @Provides
    static SivMode sivMode() {
        return new SivMode();
    }

    /**
     * Default path encryption that uses Base64-urlsafe path serialization and AES-CGM-SIV mode for encryption
     */
    @Binds
    abstract PathEncryptorDecryptor pathEncryptorDecryptor(DefaultPathEncryptorDecryptorRuntimeDelegatable impl);

    /**
     * By default simply use
     * {@link de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService} to get key
     * and pass path with key to {@link SymmetricPathEncryptionService}
     */
    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImplRuntimeDelegatable impl);

    /**
     * Default symmetric path encryption that encrypts URI segment-by-segment.
     */
    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImplRuntimeDelegatable impl);
}
