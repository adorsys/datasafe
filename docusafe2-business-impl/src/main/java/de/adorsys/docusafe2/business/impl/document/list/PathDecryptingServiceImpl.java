package de.adorsys.docusafe2.business.impl.document.list;

import de.adorsys.docusafe2.business.api.document.DocumentListService;
import de.adorsys.docusafe2.business.api.types.DFSAccess;
import de.adorsys.docusafe2.business.api.types.ListRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import java.util.stream.Stream;

public class PathDecryptingServiceImpl implements DocumentListService {

    @Inject
    public PathDecryptingServiceImpl() {
    }

    @Override
    public Stream<DFSAccess> list(ListRequest request) {
        throw new NotImplementedException();
    }
}
