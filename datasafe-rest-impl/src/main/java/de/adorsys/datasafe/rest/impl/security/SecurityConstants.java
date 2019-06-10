package de.adorsys.datasafe.rest.impl.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final String AUTH_LOGIN_URL = "/api/authenticate";

    public static final String TOKEN_HEADER = "token";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "secure-api";
    public static final String TOKEN_AUDIENCE = "secure-app";

    public static final String ROLES_NAME = "rol";
    public static final String TYPE_NAME = "typ";
}