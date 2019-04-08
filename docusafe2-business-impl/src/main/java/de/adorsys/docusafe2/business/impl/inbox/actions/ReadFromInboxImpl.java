package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.inbox.actions.ReadFromInbox;
import de.adorsys.docusafe2.business.api.inbox.dto.InboxReadRequest;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.profile.UserPublicProfile;

import javax.inject.Inject;

public class ReadFromInboxImpl implements ReadFromInbox {

    private final UserProfileService profiles;

    @Inject
    public ReadFromInboxImpl(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @Override
    public void readDocumentFromInbox(InboxReadRequest request) {
        UserPublicProfile profile = profiles.publicProfile(request.getOwner().getId());
        BucketPath inboxPath = profile.getInbox();
    }
}
