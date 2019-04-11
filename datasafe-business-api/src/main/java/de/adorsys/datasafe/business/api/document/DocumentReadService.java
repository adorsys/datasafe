package de.adorsys.datasafe.business.api.document;

import de.adorsys.datasafe.business.api.types.ReadRequest;

public interface DocumentReadService {

    void read(ReadRequest request);
}
