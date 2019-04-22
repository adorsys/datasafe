package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.directory.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.document.DocumentListService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.storage.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListInboxImpl implements ListInbox {

    private final BucketAccessService accessService;
    private final DocumentListService listService;
    private final ProfileRetrievalService profiles;

    public ListInboxImpl(BucketAccessService accessService, DocumentListService listService,
                         ProfileRetrievalService profiles) {
        this.accessService = accessService;
        this.listService = listService;
        this.profiles = profiles;
    }

    @Inject


    @Override
    public Stream<PrivateResource> list(UserIDAuth forUser) {
        PrivateResource userInbox = accessService.privateAccessFor(
                forUser,
                profiles.publicProfile(forUser.getUserID()).getInbox()
        );

        return listService.list(userInbox).map(it -> new DefaultPrivateResource(it.locationWithAccess()));
    }
}
