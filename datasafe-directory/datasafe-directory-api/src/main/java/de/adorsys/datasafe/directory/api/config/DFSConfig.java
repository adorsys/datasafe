package de.adorsys.datasafe.directory.api.config;

import de.adorsys.datasafe.types.api.resource.Uri;

/**
 * Default configuration for the case when user profiles are located on some DFS.
 */
public interface DFSConfig {

    /**
     * Password for keystore serialization/deserialization and public key retrieval.
     */
    String keystorePassword();

    /**
     * Where to store user profile data relative to system bucket.
     */
    Uri systemRoot();
}
