package de.adorsys.datasafe.types.api.resource;

import lombok.Data;

/**
 * Acts as an identifier of client resource.
 */
@Data
public class StorageIdentifier {

    public static final String DEFAULT_ID = "DEFAULT";

    // Default users' private storage
    public static final StorageIdentifier DEFAULT = new StorageIdentifier(DEFAULT_ID);

    /**
     * By default, it is prefix that will be used to match credentials to URI.
     */
    private final String id;
}
