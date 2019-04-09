package de.adorsys.docusafe2.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.inbox.actions.ListInbox;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ListRequest;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;

import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Stream;

public class ListInboxImpl implements ListInbox {

    private final BucketAccessService accessService;
    private final DocumentListService listService;

    @Inject
    public ListInboxImpl(BucketAccessService accessService, DocumentListService listService) {
        this.accessService = accessService;
        this.listService = listService;
    }

    @Override
    public Stream<DFSAccess> listInbox(UserIdAuth forUser) {
        DFSAccess userInbox = accessService.privateAccessFor(
                forUser,
                resolveInboxLocation(forUser)
        );

        return listService.list(ListRequest.builder().location(userInbox).build());
    }

    private Function<UserProfileService, BucketPath> resolveInboxLocation(UserIdAuth forUser) {
        return profiles -> profiles
                .publicProfile(forUser.getId())
                .getInbox();
    }
}
