package de.adorsys.docusafe2.business.impl.document.list;

import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.types.ListRequest;
import de.adorsys.docusafe2.business.api.types.file.FileOnBucket;

import javax.inject.Inject;
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
    public Stream<FileOnBucket> list(ListRequest request) {
        if (request.isDecryptPath()) {
            return decryptingService.list(request);
        }

        return nonDecryptingService.list(request);
    }
}