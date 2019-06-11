package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.rest.impl.security.SecurityConstants;
import de.adorsys.datasafe.rest.impl.security.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseTokenDatasafeEndpointTest extends BaseDatasafeEndpointTest {

    protected static final String TEST_USER = "test";
    protected static final String TEST_PASS = "test";
    protected String token;

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
