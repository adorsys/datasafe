package de.adorsys.datasafe.examples.business.filesystem;

import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.Uri;

import java.util.function.Function;

/**
 * This module is responsible for providing noop pathencryption of document.
 */
@Module
abstract class CustomPathEncryptionModule {

    /**
     * Disables path encryption, note that because we are rebuilding dependency graph in this example, it is not
     * needed to provide other services seen in
     * {@link de.adorsys.datasafe.business.impl.pathencryption.DefaultPathEncryptionModule}
     */
    @Provides
    static PathEncryption pathEncryption() {
        return new PathEncryption() {

            // no path encryption
            @Override
            public Uri encrypt(UserIDAuth forUser, Uri path) {
                return path;
            }

            // no path decryption
            @Override
            public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
                return Function.identity();
            }
        };
    }
}
