package de.adorsys.datasafe.inbox.impl.actions;


import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;

import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * Default listing service that locates user INBOX folder using {@link ResourceResolver}
 * and delegates request to {@link StorageListService} to list all files within it.
 */
@RuntimeDelegate
public class ListInboxImpl implements ListInbox {

    private final PrivateKeyService keyService;
    private final ProfileRetrievalService profileRetrievalService;
    private final ResourceResolver resolver;
    private final StorageListService listService;

    @Inject
    public ListInboxImpl(PrivateKeyService keyService, ProfileRetrievalService profileRetrievalService,
                         ResourceResolver resolver, StorageListService listService) {
        this.keyService = keyService;
        this.profileRetrievalService = profileRetrievalService;
        this.resolver = resolver;
        this.listService = listService;
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        keyService.validateUserHasAccessOrThrow(request.getOwner());
        return listService.list(resolveRelative(request))
                .map(it -> fillEncryptedDecryptedSegments(request, it));
    }

    private AbsoluteLocation<PrivateResource> resolveRelative(
            ListRequest<UserIDAuth, PrivateResource> request) {
        return resolver.resolveRelativeToPrivateInbox(
                request.getOwner(),
                request.getLocation()
        );
    }

    // needed only to support encrypted and decrypted path segments in result, otherwise they are empty
    private AbsoluteLocation<ResolvedResource> fillEncryptedDecryptedSegments(
            ListRequest<UserIDAuth, PrivateResource> request,
            AbsoluteLocation<ResolvedResource> resource) {
        AbsoluteLocation<PublicResource> inboxPath = profileRetrievalService
                .publicProfile(request.getOwner().getUserID()).getInbox();

        Uri path = inboxPath.location().relativize(resource.location());
        return new AbsoluteLocation<>(
                resource.getResource().withResource(resource.getResource().asPrivate().resolve(path, path))
        );
    }
}
