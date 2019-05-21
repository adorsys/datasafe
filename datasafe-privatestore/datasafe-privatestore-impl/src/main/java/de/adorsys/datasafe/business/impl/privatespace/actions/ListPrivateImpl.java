package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.privatespace.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.business.api.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.storage.actions.StorageListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.actions.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;

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
