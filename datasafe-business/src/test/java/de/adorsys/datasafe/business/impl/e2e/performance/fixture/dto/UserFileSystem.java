package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import lombok.Data;

/**
 * View of public and private users' filesystems
 */
@Data
public class UserFileSystem {

    private final TestUser user;
    private final TestFileTree privateFiles;
    private final TestFileTree inboxFiles;
}
