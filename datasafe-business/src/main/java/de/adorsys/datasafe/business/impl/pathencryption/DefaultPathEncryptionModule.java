package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathEncryptorDecryptorRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptorDecryptor;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.SymmetricPathEncryptionServiceImplRuntimeDelegatable;

/**
 * This module is responsible for providing pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    /**
     * Default path encryption that uses Base64-urlsafe path serialization
     */
    @Binds
    abstract PathEncryptorDecryptor config(DefaultPathEncryptorDecryptorRuntimeDelegatable config);

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
