package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

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
