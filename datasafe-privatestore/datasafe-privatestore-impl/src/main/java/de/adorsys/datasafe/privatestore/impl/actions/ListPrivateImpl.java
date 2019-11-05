package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.PasswordClearingStream;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;

import javax.inject.Inject;
import java.util.function.Function;

/**
 * Default listing service that encrypts the location of where to list files using {@link EncryptedResourceResolver}
 * and delegates request to {@link StorageListService} after that it decrypts obtained resources to retrieve
 * logical resource path (decrypted path) within users' privatespace.
 */
@RuntimeDelegate
public class ListPrivateImpl implements ListPrivate {

    private final EncryptedResourceResolver resolver;
    private final StorageListService listService;

    @Inject
    public ListPrivateImpl(EncryptedResourceResolver resolver, StorageListService listService) {
        this.resolver = resolver;
        this.listService = listService;
    }

    @Override
    public PasswordClearingStream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        // Access check is implicit - on keystore access in EncryptedResourceResolver
        AbsoluteLocation<PrivateResource> listDir =
                resolver.encryptAndResolvePath(
                    request.getOwner(),
                    request.getLocation(),
                    request.getStorageIdentifier()
                );

        Function<PrivateResource, AbsoluteLocation<PrivateResource>> decryptingResolver = resolver.decryptingResolver(
                request.getOwner(), listDir.getResource(), request.getStorageIdentifier()
        );

        return new PasswordClearingStream<>(listService.list(listDir).map(it -> {
            AbsoluteLocation<PrivateResource> decrypted = decryptingResolver.apply(it.getResource().asPrivate());
            return new AbsoluteLocation<>(it.getResource().withResource(decrypted.getResource()));
        }), request.getOwner().getReadKeyPassword());
    }
}
