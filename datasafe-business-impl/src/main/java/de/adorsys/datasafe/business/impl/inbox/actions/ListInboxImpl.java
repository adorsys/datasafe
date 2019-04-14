package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.deployment.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.InboxBucketPath;
import de.adorsys.datasafe.business.api.types.ListRequest;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

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
    public Stream<InboxBucketPath> list(UserIDAuth forUser) {
        DFSAccess userInbox = accessService.privateAccessFor(
                forUser,
                resolveInboxLocation(forUser)
        );

        ListRequest listRequest = ListRequest.builder()
                .location(userInbox)
                .decryptPath(false)
                .recursiveFlag(ListRecursiveFlag.FALSE)
                .build();

        return listService.list(listRequest).map(this::asInbox);
    }

    private Function<ProfileRetrievalService, BucketPath> resolveInboxLocation(UserIDAuth forUser) {
        return profiles -> profiles
                .publicProfile(forUser.getUserID())
                .getInbox();
    }

    private InboxBucketPath asInbox(FileOnBucket path) {
        return new InboxBucketPath(relativize(path.getPath()));
    }

    private BucketPath relativize(BucketPath child) {
        String[] name = child.getObjectHandle().getName().split(BucketPath.BUCKET_SEPARATOR);
        return new BucketPath(name.length == 1 ? name[0] : name[name.length - 1]);
    }
}
