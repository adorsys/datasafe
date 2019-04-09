package de.adorsys.docusafe2.business.impl.document.list;

import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ListRequest;

import javax.inject.Inject;
import java.util.stream.Stream;

public class PathNonDecryptingServiceImpl implements DocumentListService {

    @Inject
    public PathNonDecryptingServiceImpl() {
    }

    @Override
    public Stream<DFSAccess> list(ListRequest request) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
