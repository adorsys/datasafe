package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder(toBuilder = true)
@ToString
public class OperationResult {

    /**
     * Content that is expected for i.e. read operation
     */
    private final ContentId content;

    /**
     * Folder content that is expected for i.e. list operation
     */
    private final Set<String> dirContent;
}
