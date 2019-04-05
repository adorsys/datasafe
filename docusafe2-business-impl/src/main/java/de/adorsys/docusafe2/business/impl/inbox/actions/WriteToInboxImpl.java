package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.docusafe2.business.api.inbox.actions.WriteToInbox;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxWriteRequest;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;

import javax.inject.Inject;

public class WriteToInboxImpl implements WriteToInbox {

    private final UserProfileService profiles;

    @Inject
    public WriteToInboxImpl(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @Override
    public void writeDocumentToInboxOfUser(InboxWriteRequest request) {

    }
}
