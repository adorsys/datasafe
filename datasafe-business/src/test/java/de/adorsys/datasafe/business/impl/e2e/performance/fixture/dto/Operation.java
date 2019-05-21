package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import lombok.Data;

import java.net.URI;

@Data
public class Operation {

    private final String userId;
    private final OperationType type;
    private final StorageType storageType;
    private final ContentId contentId;
    private final URI location;
    private final ContentId expected;
}
