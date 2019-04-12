package de.adorsys.datasafe.business.impl.document.list;

import de.adorsys.datasafe.business.api.deployment.document.DocumentListService;
import de.adorsys.datasafe.business.api.types.ListRequest;
import de.adorsys.datasafe.business.api.types.file.FileOnBucket;

import javax.inject.Inject;
import java.util.stream.Stream;

// DEPLOYMENT
public class ListPathDecryptingServiceImpl implements DocumentListService {

    @Inject
    public ListPathDecryptingServiceImpl() {
    }

    @Override
    public Stream<FileOnBucket> list(ListRequest request) {
        // FIXME "https://github.com/adorsys/datasafe2/issues/<>"
        throw new UnsupportedOperationException("https://github.com/adorsys/datasafe2/issues/<>");
    }
}
