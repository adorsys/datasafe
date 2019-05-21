package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Fixture {

    private final List<Operation> operations;
    private final Map<String, Map<String, ContentId>> userPrivateSpace;
    private final Map<String, Map<String, ContentId>> userPublicSpace;
}
