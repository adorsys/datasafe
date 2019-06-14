package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.privatestore.api.actions.*;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.*;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultPrivateActionsModule {

    /**
     * By default encrypts URI of the document and resolves its absolute location against PRIVATE folder.
     */
    @Binds
    abstract EncryptedResourceResolver encryptedResourceResolver(EncryptedResourceResolverImplRuntimeDelegatable impl);

    /**
     * Lists files in PRIVATE folder, also provides decrypted path of the document.
     */
    @Binds
    abstract ListPrivate listPrivate(ListPrivateImplRuntimeDelegatable impl);

    /**
     * Reads and decrypts file from private folder using secret key.
     */
    @Binds
    abstract ReadFromPrivate readFromPrivate(ReadFromPrivateImplRuntimeDelegatable impl);

    /**
     * Writes and encrypts file to private folder using secret key (also document URI is encrypted using
     * {@link EncryptedResourceResolver}).
     */
    @Binds
    abstract WriteToPrivate writeToPrivate(WriteToPrivateImplRuntimeDelegatable impl);

    /**
     * Removes file from user private storage.
     */
    @Binds
    abstract RemoveFromPrivate removeFromPrivate(RemoveFromPrivateImplRuntimeDelegatable impl);

    /**
     * Aggregate view of operations that can be done on privatespace.
     */
    @Binds
    abstract PrivateSpaceService privateSpaceService(PrivateSpaceServiceImplRuntimeDelegatable impl);
}
