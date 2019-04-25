package de.adorsys.datasafe.business.impl.inbox.actions;

import de.adorsys.datasafe.business.api.inbox.actions.ListInbox;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.resource.ResourceResolver;

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
    public Stream<PrivateResource> list(ListRequest<UserIDAuth> forUser) {
        return listService.list(resolveRelative(forUser).getLocation());
    }

    private ListRequest<UserIDAuth> resolveRelative(ListRequest<UserIDAuth> request) {
        return request.toBuilder().location(
                resolver.resolveRelativeToPrivateInbox(
                        request.getOwner(),
                        request.getLocation()
                )
        ).build();
    }
}
