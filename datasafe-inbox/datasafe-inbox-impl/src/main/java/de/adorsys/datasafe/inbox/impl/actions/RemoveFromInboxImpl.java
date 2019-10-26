package de.adorsys.datasafe.inbox.impl.actions;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.actions.RemoveFromInbox;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import javax.inject.Inject;

/**
 * Default removal service that locates resource to remove using {@link ResourceResolver} and
 * directly calls {@link StorageRemoveService} to delete resolved resource within INBOX.
 */
@RuntimeDelegate
public class RemoveFromInboxImpl implements RemoveFromInbox {

    private final PrivateKeyService keyService;
    private final ResourceResolver resolver;
    private final StorageRemoveService remover;

    @Inject
    public RemoveFromInboxImpl(PrivateKeyService keyService, ResourceResolver resolver, StorageRemoveService remover) {
        this.keyService = keyService;
        this.resolver = resolver;
        this.remover = remover;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        keyService.validateUserHasAccessOrThrow(request.getOwner());
        remover.remove(resolver.resolveRelativeToPrivateInbox(request.getOwner(), request.getLocation()));
    }
}
