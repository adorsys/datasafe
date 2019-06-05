package de.adorsys.datasafe.business.impl.storage;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.actions.*;

@Module
public abstract class DefaultStorageModule {

    @Binds
    abstract StorageListService storageList(StorageService storageService);

    @Binds
    abstract StorageReadService readService(StorageService storageService);

    @Binds
    abstract StorageWriteService writeService(StorageService storageService);

    @Binds
    abstract StorageRemoveService removeService(StorageService storageService);

    @Binds
    abstract StorageCheckService checkService(StorageService storageService);
}
