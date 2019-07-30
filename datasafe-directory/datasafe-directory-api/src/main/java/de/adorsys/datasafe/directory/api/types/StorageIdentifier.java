package de.adorsys.datasafe.directory.api.types;

import lombok.Data;

/**
 * Acts as an identifier for {@link StorageCredentials}.
 */
@Data
public class StorageIdentifier {

    /**
     * By default, it is prefix that will be used to match credentials to URI.
     */
    private final String id;
}
