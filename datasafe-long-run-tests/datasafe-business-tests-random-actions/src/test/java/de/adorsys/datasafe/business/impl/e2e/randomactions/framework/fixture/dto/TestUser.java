package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents user that performs some operations on virtual tree.
 */
@Data
@EqualsAndHashCode(of = "username")
public class TestUser {

    private final String username;
    private final String password;
    private final UserIDAuth auth;

    public TestUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.auth = new UserIDAuth(new UserID(username), ReadKeyPasswordTestFactory.getForString(password));
    }
}
