package de.adorsys.datasafe.business.impl.privatestore.actions;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.privatestore.api.actions.ReadFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.privatestore.api.actions.WriteToPrivate;
import de.adorsys.datasafe.privatestore.impl.PrivateSpaceServiceImpl;
import de.adorsys.datasafe.privatestore.impl.actions.EncryptedResourceResolverImpl;
import de.adorsys.datasafe.privatestore.impl.actions.ListPrivateImpl;
import de.adorsys.datasafe.privatestore.impl.actions.ReadFromPrivateImpl;
import de.adorsys.datasafe.privatestore.impl.actions.RemoveFromPrivateImpl;
import de.adorsys.datasafe.privatestore.impl.actions.WriteToPrivateImpl;

/**
 * This module is responsible for providing default actions on PRIVATE folder.
 */
@Module
public abstract class DefaultPrivateActionsModule {

    /**
     * By default encrypts URI of the document and resolves its absolute location against PRIVATE folder.
     */
    @Binds
    abstract EncryptedResourceResolver encryptedResourceResolver(EncryptedResourceResolverImpl impl);

    /**
     * Lists files in PRIVATE folder, also provides decrypted path of the document.
     */
    @Binds
    abstract ListPrivate listPrivate(ListPrivateImpl impl);

    /**
     * Reads and decrypts file from private folder using secret key.
     */
    @Binds
    abstract ReadFromPrivate readFromPrivate(ReadFromPrivateImpl impl);

    /**
     * Writes and encrypts file to private folder using secret key (also document URI is encrypted using
     * {@link EncryptedResourceResolver}).
     */
    @Binds
    abstract WriteToPrivate writeToPrivate(WriteToPrivateImpl impl);

    /**
     * Removes file from user private storage.
     */
    @Binds
    abstract RemoveFromPrivate removeFromPrivate(RemoveFromPrivateImpl impl);

    /**
     * Aggregate view of operations that can be done on privatespace.
     */
    @Binds
    abstract PrivateSpaceService privateSpaceService(PrivateSpaceServiceImpl impl);
}
