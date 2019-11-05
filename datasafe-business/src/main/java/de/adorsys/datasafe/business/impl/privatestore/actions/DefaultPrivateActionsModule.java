package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.EncryptedResourceResolverImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.ListPrivateImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.ReadFromPrivateImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.RemoveFromPrivateImplRuntimeDelegatable;
import de.adorsys.datasafe.privatestore.impl.actions.WriteToPrivateImplRuntimeDelegatable;

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
