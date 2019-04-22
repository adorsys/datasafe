package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentListService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentReadService;
import de.adorsys.datasafe.business.impl.document.cms.CMSDocumentWriteService;

/**
 * This module is responsible for document storage (i.e. which pathencryption to use) and listing bucket content.
 */
@Module
public abstract class DefaultDocumentModule {

    @Binds
    abstract EncryptedDocumentReadService documentReadService(CMSDocumentReadService impl);

    @Binds
    abstract EncryptedDocumentWriteService documentWriteService(CMSDocumentWriteService impl);

    @Binds
    abstract EncryptedDocumentListService documentListService(DefaultEncryptedPathListService impl);
}
