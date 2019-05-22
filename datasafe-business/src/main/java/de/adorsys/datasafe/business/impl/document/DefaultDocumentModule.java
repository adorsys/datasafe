package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;
import de.adorsys.datasafe.business.impl.encryption.document.CMSDocumentReadService;
import de.adorsys.datasafe.business.impl.encryption.document.CMSDocumentWriteService;

/**
 * This module is responsible for document storage (i.e. which pathencryption to use) and listing bucket content.
 */
@Module
public abstract class DefaultDocumentModule {

    @CoberturaIgnore
    @Binds
    abstract EncryptedDocumentReadService documentReadService(CMSDocumentReadService impl);

    @CoberturaIgnore
    @Binds
    abstract EncryptedDocumentWriteService documentWriteService(CMSDocumentWriteService impl);
}
