package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.rest.impl.security.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@PropertySource("classpath:jwt-config.properties")
public class BaseTokenDatasafeEndpointTest extends BaseDatasafeEndpointTest {

    static final String TEST_USER = "test";
    static final String TEST_PASS = "test";
    String token;

    @Autowired
    Environment env;

    @BeforeEach
    public void setup() {
        UserDTO userDTO = new UserDTO(env.getProperty("default_user"), env.getProperty("default_password"));

        token = sendAuthenticateRequest(userDTO).getResponse().getHeader(SecurityConstants.TOKEN_HEADER);
    }
}
