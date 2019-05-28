package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "username")
public class TestUser {

    private final String username;
    private final String password;
    private final UserIDAuth auth;

    public TestUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.auth = new UserIDAuth(new UserID(username), new ReadKeyPassword(password));
    }
}
