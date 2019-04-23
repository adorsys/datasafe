package de.adorsys.datasafe.business.impl.testcontainers;

import dagger.Component;
import de.adorsys.datasafe.business.api.deployment.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.datasafe.business.impl.credentials.DefaultCredentialsModule;
import de.adorsys.datasafe.business.impl.dfs.DefaultDFSModule;
import de.adorsys.datasafe.business.impl.document.DefaultDocumentModule;
import de.adorsys.datasafe.business.impl.inbox.actions.DefaultInboxActionsModule;
import de.adorsys.datasafe.business.impl.keystore.DefaultKeyStoreModule;
import de.adorsys.datasafe.business.impl.privatestore.actions.DefaultPrivateActionsModule;
import de.adorsys.datasafe.business.impl.profile.DefaultProfileModule;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeServices;

import javax.inject.Singleton;

/**
 * This is user Datasafe service test implementation.
 * With fake path module for verifying that encryption is used
 */
@Singleton
@Component(modules = {
        DefaultCredentialsModule.class,
        DefaultKeyStoreModule.class,
        DefaultDocumentModule.class,
        DefaultDFSModule.class,
        DefaultCMSEncryptionModule.class,
        FakePathEncryptionModule.class,
        DefaultInboxActionsModule.class,
        DefaultPrivateActionsModule.class,
        DefaultProfileModule.class
})
public interface TestDocusafeServices extends DefaultDocusafeServices {

    PathEncryption pathEncryption();
}
