package de.adorsys.datasafe.business.api.deployment.document;

import de.adorsys.datasafe.business.api.types.action.WriteRequest;

import java.io.OutputStream;

public interface DocumentWriteService {

    OutputStream write(WriteRequest request);
}


