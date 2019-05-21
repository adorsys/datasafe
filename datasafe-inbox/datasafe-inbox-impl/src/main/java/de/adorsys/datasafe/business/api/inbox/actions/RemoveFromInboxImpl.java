package de.adorsys.datasafe.business.api.inbox.actions;

import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.storage.actions.StorageRemoveService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.RemoveRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;

public class RemoveFromInboxImpl implements RemoveFromInbox {

    private final ResourceResolver resolver;
    private final StorageRemoveService remover;

    @Inject
    public RemoveFromInboxImpl(ResourceResolver resolver, StorageRemoveService remover) {
        this.resolver = resolver;
        this.remover = remover;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        remover.remove(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation()));
    }
}