package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.util.Set;

@Getter
@Builder
public class OperationResult {

    /**
     * Content that is expected for i.e. read operation
     */
    private final ContentId content;

    /**
     * Folder content that is expected for i.e. list operation
     */
    private final Set<URI> dirContent;
}
