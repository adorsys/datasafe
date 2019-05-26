package de.adorsys.datasafe.inbox.impl.actions;


import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.inbox.api.actions.ListInbox;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListInboxImpl implements ListInbox {

    private final ResourceResolver resolver;
    private final StorageListService listService;

    @Inject
    public ListInboxImpl(ResourceResolver resolver, StorageListService listService) {
        this.resolver = resolver;
        this.listService = listService;
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> forUser) {
        return listService.list(resolveRelative(forUser));
    }

    private AbsoluteLocation<PrivateResource> resolveRelative(
            ListRequest<UserIDAuth, PrivateResource> request) {
        return resolver.resolveRelativeToPrivateInbox(
                request.getOwner(),
                request.getLocation()
        );
    }
}
