package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;

public interface PrivateResource extends ResourceLocation<PrivateResource> {

    /**
     * Physical path for path-encrypted document
     * @return encrypted relative URI
     */
    URI encryptedPath();

    /**
     * Logical path for path-decrypted document
     * @return decrypted relative URI
     */
    URI decryptedPath();

    PrivateResource resolve(URI encryptedPath, URI decryptedPath);
}
