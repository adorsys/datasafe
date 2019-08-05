package de.adorsys.datasafe.directory.api.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Credentials to access storage system (i.e. s3 accessKeyId/secretKey).
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class StorageCredentials {

    private final String username;
    private final String password;
}
