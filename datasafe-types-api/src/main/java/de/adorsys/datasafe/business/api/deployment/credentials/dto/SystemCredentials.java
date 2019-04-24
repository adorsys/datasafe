package de.adorsys.datasafe.business.api.deployment.credentials.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
public class SystemCredentials implements DFSCredentials {

    @Getter
    private final CredentialsType type = CredentialsType.SYSTEM;
    private final String id;
}
