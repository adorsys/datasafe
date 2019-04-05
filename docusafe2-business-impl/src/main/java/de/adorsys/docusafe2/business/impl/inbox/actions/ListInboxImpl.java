package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.docusafe2.business.api.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.InboxBucketPath;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListInboxImpl implements ListInbox {

    private final UserProfileService profiles;

    @Inject
    public ListInboxImpl(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @Override
    public Stream<InboxBucketPath> listInbox(UserIdAuth forUser) {
        return null;
    }
}
