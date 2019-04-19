package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.deployment.credentials.BucketAccessService;
import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.deployment.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.deployment.profile.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import javax.inject.Inject;
import java.net.URI;
import java.util.function.Function;
import java.util.stream.Stream;

public class ListPrivateImpl implements ListPrivate {

    private final BucketAccessService accessService;
    private final DocumentListService listService;

    @Inject
    public ListPrivateImpl(BucketAccessService accessService, DocumentListService listService) {
        this.accessService = accessService;
        this.listService = listService;
    }

    @Override
    public Stream<URI> list(UserIDAuth forUser) {
        DFSAccess userPrivate = accessService.privateAccessFor(
                forUser,
                resolvePrivateLocation(forUser)
        );

        ListRequest listRequest = ListRequest.builder()
                .location(userPrivate)
                .decryptPath(true)
                .recursiveFlag(ListRecursiveFlag.FALSE)
                .build();

        return listService.list(listRequest);
    }

    private Function<ProfileRetrievalService, URI> resolvePrivateLocation(UserIDAuth forUser) {
        return profiles -> profiles
                .privateProfile(forUser)
                .getPrivateStorage();
    }
}
