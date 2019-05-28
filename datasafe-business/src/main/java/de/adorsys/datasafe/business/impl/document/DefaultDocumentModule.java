package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;

/**
 * This module is responsible for document storage (i.e. which pathencryption to use) and listing bucket content.
 */
@Module
public abstract class DefaultDocumentModule {

    @Binds
    abstract EncryptedDocumentReadService documentReadService(CMSDocumentReadService impl);

    @Binds
    abstract EncryptedDocumentWriteService documentWriteService(CMSDocumentWriteService impl);
}
