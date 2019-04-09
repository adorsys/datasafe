package de.adorsys.docusafe2.business.impl.document;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.document.DocumentReadService;
import de.adorsys.docusafe2.business.api.document.DocumentWriteService;
import de.adorsys.docusafe2.business.impl.document.cms.CMSDocumentReadService;
import de.adorsys.docusafe2.business.impl.document.cms.CMSDocumentWriteService;
import de.adorsys.docusafe2.business.impl.document.list.DocumentListServiceImpl;
import de.adorsys.docusafe2.business.impl.document.list.PathDecryptingServiceImpl;
import de.adorsys.docusafe2.business.impl.document.list.PathNonDecryptingServiceImpl;

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
    static PathDecryptingServiceImpl pathDecryptingService() {
        return new PathDecryptingServiceImpl();
    }

    @Provides
    static PathNonDecryptingServiceImpl pathNonDecryptingService() {
        return new PathNonDecryptingServiceImpl();
    }
}
