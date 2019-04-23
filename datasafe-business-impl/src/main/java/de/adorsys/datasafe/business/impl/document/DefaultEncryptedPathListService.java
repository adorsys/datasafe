package de.adorsys.datasafe.business.impl.document;

import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentListService;
import de.adorsys.datasafe.business.api.storage.StorageListService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.util.stream.Stream;

public class DefaultEncryptedPathListService implements EncryptedDocumentListService {

    private final StorageListService listService;

    @Inject
    public DefaultEncryptedPathListService(StorageListService listService) {
        this.listService = listService;
    }

    @Override
    public Stream<PrivateResource> list(ListRequest<UserIDAuth> location) {
        return listService.list(location.getLocation());
    }
}
