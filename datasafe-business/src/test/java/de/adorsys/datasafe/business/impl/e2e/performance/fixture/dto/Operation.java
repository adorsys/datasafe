package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import lombok.Data;

import java.net.URI;

/**
 * Represents operation done on DFS.
 */
@Data
public class Operation {

    /**
     * Who did the operation.
     */
    private final String userId;

    /**
     * What kind of operation - read/write...
     */
    private final OperationType type;

    /**
     * On what storage - private or public
     */
    private final StorageType storageType;

    /**
     * What content id to expect in storage (identifier of what was written or is expected to be read)
     */
    private final ContentId contentId;

    /**
     *
     */
    private final URI location;

    /**
     *
     */
    private final ContentId expected;
}
