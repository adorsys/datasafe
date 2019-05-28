package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;

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
