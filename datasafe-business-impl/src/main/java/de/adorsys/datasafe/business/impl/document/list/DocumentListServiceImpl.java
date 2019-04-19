package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.action.ListRequest;

import javax.inject.Inject;
import java.net.URI;
import java.util.stream.Stream;

/**
 * List available documents within DFS, decrypting their logical path if necessary.
 */
public class DocumentListServiceImpl implements DocumentListService {

    private final ListPathDecryptingServiceImpl decryptingService;
    private final ListPathNonDecryptingServiceImpl nonDecryptingService;

    @Inject
    public DocumentListServiceImpl(
            ListPathDecryptingServiceImpl decryptingService,
            ListPathNonDecryptingServiceImpl nonDecryptingService) {
        this.decryptingService = decryptingService;
        this.nonDecryptingService = nonDecryptingService;
    }

    @Override
    public Stream<URI> list(ListRequest request) {
        if (request.isDecryptPath()) {
            return decryptingService.list(request);
        }

        return nonDecryptingService.list(request);
    }
}
