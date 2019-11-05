package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.rest.impl.security.SecurityConstants;
import de.adorsys.datasafe.rest.impl.security.SecurityProperties;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;

@AutoConfigureRestDocs(uriHost = "example.com/datasafe", uriPort = 80)
public abstract class BaseTokenDatasafeEndpointTest extends BaseDatasafeEndpointTest {
    static final String TOKEN_DESCRIPTION = "Bearer authentication token is required";
    static final String USER_DESCRIPTION = "datasafe username";
    static final String PASSWORD_DESCRIPTION = "datasafe user's password";

    static final String TEST_USER = "test";
    static final ReadKeyPassword TEST_PASS = ReadKeyPasswordHelper.getForString("test");
    String token;

    private SecurityProperties securityProperties;

    @Autowired
    public final void setSecurityProperties(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @BeforeEach
    public void init() {
        UserDTO userDTO = new UserDTO(securityProperties.getDefaultUser(), securityProperties.getDefaultPassword());
        token = sendAuthenticateRequest(userDTO).getResponse().getHeader(SecurityConstants.TOKEN_HEADER);
    }
}
