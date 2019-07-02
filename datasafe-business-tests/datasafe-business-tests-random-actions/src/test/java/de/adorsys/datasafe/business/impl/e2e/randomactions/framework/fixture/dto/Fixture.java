package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Set of operations done by the users.
 */
@Data
public class Fixture {

    /**
     * Operation list - for example READ/WRITE/DELETE...
     */
    private final List<Operation> operations;

    /**
     * Expected resulting privatespace for each user as map username-[path, file content id].
     */
    private final Map<String, Map<String, ContentId>> userPrivateSpace;

    /**
     * Expected resulting publicspace for each user as map username-[path, file content id].
     */
    private final Map<String, Map<String, ContentId>> userPublicSpace;
}
