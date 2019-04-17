package de.adorsys.datasafe.business.api.deployment.document;

import de.adorsys.datasafe.business.api.types.action.ReadRequest;

public interface DocumentReadService {

    void read(ReadRequest request);
}
