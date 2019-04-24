package de.adorsys.datasafe.business.api.deployment.document;

import java.io.OutputStream;

import de.adorsys.datasafe.business.api.types.action.ObjectMetadata;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;

public interface DocumentWriteService {

    OutputStream write(WriteRequest request);

    default OutputStream write(WriteRequest request, ObjectMetadata metadata) {
    	throw new UnsupportedOperationException("Still to be implemented.");
    }
}


