package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.RemoveFromPrivate;
import de.adorsys.datasafe.storage.api.actions.StorageRemoveService;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

import javax.inject.Inject;

/**
 * Default removal service that locates resource to remove using {@link EncryptedResourceResolver} and
 * directly calls {@link StorageRemoveService} to delete resolved resource
 */
@RuntimeDelegate
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
        // Access check is implicit - on keystore access in EncryptedResourceResolver
        remover.remove(resolver.encryptAndResolvePath(
            request.getOwner(),
            request.getLocation(),
            request.getStorageIdentifier())
        );
        request.getOwner().getReadKeyPassword().clear();
    }

    @Override
    public void makeSurePasswordClearanceIsDone() {

    }
}
