package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.api.privatespace.actions.RemoveFromPrivate;
import de.adorsys.datasafe.business.api.storage.actions.StorageRemoveService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.RemoveRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;

public class RemoveFromPrivateImpl implements RemoveFromPrivate {
    private final EncryptedResourceResolver resolver;
    private final StorageRemoveService remover;

    @Inject
    public RemoveFromPrivateImpl(EncryptedResourceResolver resolver, StorageRemoveService remover) {
        this.resolver = resolver;
        this.remover = remover;
    }

    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        remover.remove(resolver.encryptAndResolvePath(request.getOwner(), request.getLocation()));
    }
}
