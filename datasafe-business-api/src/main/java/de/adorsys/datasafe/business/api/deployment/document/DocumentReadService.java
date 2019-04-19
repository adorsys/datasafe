package de.adorsys.datasafe.business.api.deployment.document;

import de.adorsys.datasafe.business.api.types.action.ReadRequest;

import java.io.InputStream;

public interface DocumentReadService {

    InputStream read(ReadRequest request);
}
