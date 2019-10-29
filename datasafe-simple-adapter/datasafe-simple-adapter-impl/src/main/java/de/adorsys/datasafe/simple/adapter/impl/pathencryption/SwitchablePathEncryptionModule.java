package de.adorsys.datasafe.simple.adapter.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;

/**
 * This module is responsible for providing pathencryption of document.
 */
@Module
public abstract class SwitchablePathEncryptionModule {
       /**
         * By default simply use
         * {@link de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService} to get key
         * and pass path with key to {@link SwitchablePathEncryptionImpl}
         */
        @Binds
        abstract PathEncryption pathEncryption(SwitchablePathEncryptionImpl impl);
}
