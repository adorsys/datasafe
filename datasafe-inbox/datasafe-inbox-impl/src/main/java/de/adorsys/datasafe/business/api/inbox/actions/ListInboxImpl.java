package de.adorsys.datasafe.business.api.inbox.actions;


import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.storage.actions.StorageListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;

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
