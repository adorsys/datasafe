package de.adorsys.datasafe.business.impl.privatestore.actions;

import de.adorsys.datasafe.business.api.directory.privatespace.actions.ListPrivate;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;

import javax.inject.Inject;
import java.util.stream.Stream;

public class ListPrivateImpl implements ListPrivate {

    private final EncryptedDocumentListService listService;

    @Inject
    public ListPrivateImpl(EncryptedDocumentListService listService) {
        this.listService = listService;
    }

    @Override
    public Stream<PrivateResource> list(ListRequest<UserIDAuth> request) {
        return listService.list(request).map(it -> new DefaultPrivateResource(it.locationWithAccess()));
    }
}
