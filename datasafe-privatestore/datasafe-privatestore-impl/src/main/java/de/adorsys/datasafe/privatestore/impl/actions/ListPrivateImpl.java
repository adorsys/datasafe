package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.privatestore.api.actions.ListPrivate;
import de.adorsys.datasafe.storage.api.actions.StorageListService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListPrivateImpl implements ListPrivate {

    private final EncryptedResourceResolver resolver;
    private final StorageListService listService;

    @Inject
    public ListPrivateImpl(EncryptedResourceResolver resolver, StorageListService listService) {
        this.resolver = resolver;
        this.listService = listService;
    }

    @Override
    public Stream<AbsoluteLocation<ResolvedResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        AbsoluteLocation<PrivateResource> listDir =
                resolver.encryptAndResolvePath(request.getOwner(), request.getLocation());

        return listService
                .list(listDir)
                .map(it -> decryptPath(request.getOwner(), it, listDir.getResource()));
    }

    private AbsoluteLocation<ResolvedResource> decryptPath(
            UserIDAuth owner, AbsoluteLocation<ResolvedResource> resource, PrivateResource root) {

        AbsoluteLocation<PrivateResource> decrypted = resolver.decryptAndResolvePath(
                owner,
                resource.getResource().asPrivate(),
                root
        );

        return new AbsoluteLocation<>(resource.getResource().withResource(decrypted.getResource()));
    }
}
