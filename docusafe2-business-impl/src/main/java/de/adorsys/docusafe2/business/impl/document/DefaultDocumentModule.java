package de.adorsys.docusafe2.business.impl.document;

import dagger.Binds;
import dagger.Module;
import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.document.DocumentReadService;
import de.adorsys.docusafe2.business.api.document.DocumentWriteService;
import de.adorsys.docusafe2.business.impl.document.cms.CMSDocumentReadService;
import de.adorsys.docusafe2.business.impl.document.cms.CMSDocumentWriteService;
import de.adorsys.docusafe2.business.impl.document.list.DocumentListServiceImpl;

@Module
public abstract class DefaultDocumentModule {

    @Binds
    abstract DocumentReadService documentReadService(CMSDocumentReadService impl);

    @Binds
    abstract DocumentWriteService documentWriteService(CMSDocumentWriteService impl);

    @Binds
    abstract DocumentListService documentListService(DocumentListServiceImpl impl);
}
