package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentReadService;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentWriteService;
import de.adorsys.datasafe.business.impl.document.list.DocumentListServiceImpl;
import de.adorsys.datasafe.business.impl.document.list.ListPathDecryptingServiceImpl;
import de.adorsys.datasafe.business.impl.document.list.ListPathNonDecryptingServiceImpl;
import de.adorsys.datasafe.business.api.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.document.DocumentListService;
import de.adorsys.datasafe.business.api.document.DocumentReadService;
import de.adorsys.datasafe.business.api.document.DocumentWriteService;

/**
 * This module is responsible for document storage (i.e. which encryption to use) and listing bucket content.
 */
@Module
public abstract class DefaultDocumentModule {

    @Binds
    abstract DocumentReadService documentReadService(CMSDocumentReadService impl);

    @Binds
    abstract DocumentWriteService documentWriteService(CMSDocumentWriteService impl);

    @Binds
    abstract DocumentListService documentListService(DocumentListServiceImpl impl);

    @Provides
    static ListPathDecryptingServiceImpl pathDecryptingService() {
        return new ListPathDecryptingServiceImpl();
    }

    @Provides
    static ListPathNonDecryptingServiceImpl pathNonDecryptingService(
            StorageMetadataMapper mapper,
            DFSConnectionService dfs) {
        return new ListPathNonDecryptingServiceImpl(dfs, mapper);
    }

    @Provides
    static StorageMetadataMapper storageMetadataMapper() {
        return new StorageMetadataMapper();
    }
}
