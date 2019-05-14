package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.storage.actions.StorageListService;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

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
    public Stream<AbsoluteResourceLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        AbsoluteResourceLocation<PrivateResource> listDir =
                resolver.encryptAndResolvePath(request.getOwner(), request.getLocation());

        return listService
                .list(listDir)
                .map(it -> resolver.decryptAndResolvePath(request.getOwner(), it.getResource(), listDir.getResource()));
    }
}
