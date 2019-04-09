package de.adorsys.docusafe2.business.api.document;

import de.adorsys.docusafe2.business.api.types.ListRequest;
import de.adorsys.docusafe2.business.api.types.file.FileOnBucket;

import java.util.stream.Stream;

public interface DocumentListService {

    /**
     * Lists bucket contents.
     * @param request bucket descriptor where to list data
     * @return stream of available bucket paths
     */
    Stream<FileOnBucket> list(ListRequest request);
}
