package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.business.api.storage.dfs.DFSConnectionService;
import de.adorsys.datasafe.business.api.storage.document.DocumentListService;
import de.adorsys.datasafe.business.api.storage.document.DocumentReadService;
import de.adorsys.datasafe.business.api.storage.document.DocumentWriteService;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentReadService;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentWriteService;
import de.adorsys.datasafe.business.impl.document.list.DocumentListServiceImpl;
import de.adorsys.datasafe.business.impl.document.list.ListPathDecryptingServiceImpl;
import de.adorsys.datasafe.business.impl.document.list.ListPathNonDecryptingServiceImpl;

/**
 * This module is responsible for document storage (i.e. which pathencryption to use) and listing bucket content.
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
    static ListPathDecryptingServiceImpl pathDecryptingService(ListPathNonDecryptingServiceImpl nonDecrypt) {
        return new ListPathDecryptingServiceImpl(nonDecrypt);
    }

    @Provides
    static ListPathNonDecryptingServiceImpl pathNonDecryptingService(
            DFSConnectionService dfs) {
        return new ListPathNonDecryptingServiceImpl(dfs);
    }
}
