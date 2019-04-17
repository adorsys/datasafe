package de.adorsys.datasafe.business.api.deployment.credentials.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class S3Credentials implements DFSCredentials {

    @Getter
    private final CredentialsType type = CredentialsType.USER;
    private final String id;
}
