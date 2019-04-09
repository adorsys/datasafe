package de.adorsys.docusafe2.business.api.document;

import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ListRequest;

import java.util.stream.Stream;

public interface DocumentListService {

    /**
     * Lists bucket contents.
     * @param request bucket descriptor where to list data
     * @return stream of available bucket paths
     */
    Stream<DFSAccess> list(ListRequest request);
}
