package de.adorsys.datasafe.business.api.deployment.document;

import de.adorsys.datasafe.business.api.types.action.ListRequest;

import java.net.URI;
import java.util.stream.Stream;

public interface DocumentListService {

    /**
     * Lists bucket contents.
     * @param request bucket descriptor where to list data
     * @return stream of available bucket paths
     */
    Stream<URI> list(ListRequest request);
}
