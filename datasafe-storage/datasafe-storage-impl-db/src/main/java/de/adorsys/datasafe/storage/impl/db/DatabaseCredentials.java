package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@Data
@RequiredArgsConstructor
public class DatabaseCredentials {

    private final String username;
    private final String password;

    /**
     * Extracts credentials from URI with user info.
     */
    public DatabaseCredentials(AbsoluteLocation location) {
        URI uri = location.location().asURI();

        if (uri.getPath() == null) {
            throw new IllegalArgumentException("Wrong url format");
        }

        String[] userInfo = location.location().asURI().getUserInfo().split(":");
        this.username = userInfo[0];
        this.password = userInfo[1];
    }
}