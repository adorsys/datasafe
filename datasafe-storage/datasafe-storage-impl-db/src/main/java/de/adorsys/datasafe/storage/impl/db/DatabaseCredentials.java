package de.adorsys.datasafe.storage.impl.db;

import lombok.Data;

@Data
public class DatabaseCredentials {
    private String url;
    private String username;
    private String password;
}
