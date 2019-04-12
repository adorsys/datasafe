package de.adorsys.datasafe.business.api.deployment.document;

import de.adorsys.datasafe.business.api.types.WriteRequest;

public interface DocumentWriteService {

    void write(WriteRequest request);
}


