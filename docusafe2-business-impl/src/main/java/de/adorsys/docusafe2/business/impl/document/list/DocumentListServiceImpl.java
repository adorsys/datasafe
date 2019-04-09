package de.adorsys.docusafe2.business.impl.document.list;

import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ListRequest;

import javax.inject.Inject;
import java.util.stream.Stream;

public class DocumentListServiceImpl implements DocumentListService {

    @Inject
    public DocumentListServiceImpl() {
    }

    @Override
    public Stream<DFSAccess> list(ListRequest request) {
        return null;
    }
}
