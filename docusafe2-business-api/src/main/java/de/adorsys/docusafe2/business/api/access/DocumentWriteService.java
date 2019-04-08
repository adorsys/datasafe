package de.adorsys.docusafe2.business.api.access;

import de.adorsys.docusafe2.business.api.types.WriteRequest;

public interface DocumentWriteService {

    void write(WriteRequest request);
}


