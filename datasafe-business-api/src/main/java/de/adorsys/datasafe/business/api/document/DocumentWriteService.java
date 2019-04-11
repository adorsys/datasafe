package de.adorsys.datasafe.business.api.document;

import de.adorsys.datasafe.business.api.types.WriteRequest;

public interface DocumentWriteService {

    void write(WriteRequest request);
}


