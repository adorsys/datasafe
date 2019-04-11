package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;
import de.adorsys.datasafe.business.api.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.document.DocumentListService;
import de.adorsys.datasafe.business.api.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.profile.UserProfileService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.ListRequest;
import de.adorsys.datasafe.business.api.types.UserIdAuth;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;

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
    public Stream<FileOnBucket> listInbox(UserIdAuth forUser) {
        DFSAccess userInbox = accessService.privateAccessFor(
                forUser,
                resolveInboxLocation(forUser)
        );

        ListRequest listRequest = ListRequest.builder()
                .location(userInbox)
                .decryptPath(false)
                .recursiveFlag(ListRecursiveFlag.FALSE)
                .build();

        return listService.list(listRequest);
    }

    private Function<UserProfileService, BucketPath> resolveInboxLocation(UserIdAuth forUser) {
        return profiles -> profiles
                .publicProfile(forUser.getId())
                .getInbox();
    }
}
