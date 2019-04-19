package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.action.ListRequest;

import javax.inject.Inject;
import java.net.URI;
import java.util.stream.Stream;

// DEPLOYMENT
public class ListPathDecryptingServiceImpl implements DocumentListService {

    private final ListPathNonDecryptingServiceImpl listPathNonDecryptingService;

    @Inject
    public ListPathDecryptingServiceImpl(ListPathNonDecryptingServiceImpl listPathNonDecryptingService) {
        this.listPathNonDecryptingService = listPathNonDecryptingService;
    }

    @Override
    public Stream<URI> list(ListRequest request) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>" - should decrypt path
        return listPathNonDecryptingService.list(request);
    }
}
