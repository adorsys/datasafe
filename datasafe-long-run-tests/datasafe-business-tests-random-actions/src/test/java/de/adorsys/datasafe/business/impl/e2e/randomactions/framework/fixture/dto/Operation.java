package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

/**
 * Represents operation done on DFS.
 */
@Getter
@Builder(toBuilder = true)
@ToString
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
     * Where to write content
     */
    private final String location;

    /**
     * Recipients for sharing operation.
     */
    private final Set<String> recipients;

    /**
     * Expected operation result
     */
    private final OperationResult result;
}
