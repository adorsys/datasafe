package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * View of public and private users' filesystems
 */
@Data
@EqualsAndHashCode(of = "user")
public class UserFileSystem {

    private final TestUser user;
    private final TestFileTreeOper privateOper;
    private final TestFileTreeOper inboxOper;
}
