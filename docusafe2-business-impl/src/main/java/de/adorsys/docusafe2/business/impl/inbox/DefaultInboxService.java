package de.adorsys.docusafe2.business.impl.inbox;

import dagger.BindsInstance;
import dagger.Component;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.impl.cmsencryption.DefaultCMSEncryptionModule;
import de.adorsys.docusafe2.business.impl.credentials.DefaultCredentialsModule;
import de.adorsys.docusafe2.business.impl.dfs.DefaultDFSModule;
import de.adorsys.docusafe2.business.impl.document.cms.DefaultDocumentModule;
import de.adorsys.docusafe2.business.impl.inbox.impl.InboxServiceImpl;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        DefaultCredentialsModule.class, DefaultDocumentModule.class, DefaultDFSModule.class,
        DefaultCMSEncryptionModule.class
})
public interface DefaultInboxService {

    InboxServiceImpl inboxService();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder userProfile(UserProfileService userProfileService);

        DefaultInboxService build();
    }
}
