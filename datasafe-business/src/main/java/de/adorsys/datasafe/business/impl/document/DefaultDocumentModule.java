package de.adorsys.datasafe.business.impl.document;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadServiceRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteServiceRuntimeDelegatable;

/**
 * This module is responsible for document storage (example: which pathencryption to use) and listing bucket content.
 */
@Module
public abstract class DefaultDocumentModule {

    /**
     * By default, encrypt document using CMS-encryption provided by BouncyCastle.
     */
    @Binds
    abstract EncryptedDocumentReadService documentReadService(CMSDocumentReadServiceRuntimeDelegatable impl);

    /**
     * By default, decrypt document using CMS-encryption provided by BouncyCastle.
     */
    @Binds
    abstract EncryptedDocumentWriteService documentWriteService(CMSDocumentWriteServiceRuntimeDelegatable impl);
}
