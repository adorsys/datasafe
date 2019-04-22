package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.encryption.document.DocumentListService;
import de.adorsys.datasafe.business.api.storage.dfs.BucketAccessService;
import de.adorsys.datasafe.business.api.storage.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListPrivateImpl implements ListPrivate {

    private final BucketAccessService accessService;
    private final DocumentListService listService;
    private final ProfileRetrievalService profiles;

    @Inject
    public ListPrivateImpl(BucketAccessService accessService, DocumentListService listService,
                           ProfileRetrievalService profiles) {
        this.accessService = accessService;
        this.listService = listService;
        this.profiles = profiles;
    }

    @Override
    public Stream<PrivateResource> list(UserIDAuth forUser) {
        PrivateResource userPrivate = accessService.privateAccessFor(
                forUser,
                profiles.privateProfile(forUser).getPrivateStorage()
        );

        return listService.list(userPrivate).map(it -> new DefaultPrivateResource(it.locationWithAccess()));
    }
}
