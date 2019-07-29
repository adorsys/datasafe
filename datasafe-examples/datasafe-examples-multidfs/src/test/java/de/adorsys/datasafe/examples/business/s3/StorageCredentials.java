package de.adorsys.datasafe.examples.business.s3;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class StorageCredentials {

    private final String endpointUri;
    private final String username;
    private final String password;

    StorageCredentials(String credentialStr) {
        String[] hostUsernamePassword = credentialStr.split("\0");
        this.endpointUri = hostUsernamePassword[0];
        this.username = hostUsernamePassword[1];
        this.password = hostUsernamePassword[2];
    }

    String serialize() {
        return endpointUri + "\0" + username + "\0" + password;
    }
}
