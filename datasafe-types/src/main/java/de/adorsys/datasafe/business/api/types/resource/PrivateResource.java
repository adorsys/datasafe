package de.adorsys.datasafe.business.api.types.resource;

import java.net.URI;

public interface PrivateResource extends ResourceLocation<PrivateResource> {

    /**
     * Physical path for path-encrypted document
     * @return encrypted relative URI
     */
    URI encryptedPath();

    /**
     * Logical path for path-encrypted document
     * @return decrypted relative URI
     */
    URI decryptedPath();

    /**
     * Rebases/resolves decrypted/encrypted path against new resource root.
     * @param encryptedPath encrypted URI value, should be used to construct absolute URI.
     * @param decryptedPath decrypted URI value, used for easy navigation
     * @return new private resource that points to resource identified by encryptedPath
     */
    PrivateResource resolve(URI encryptedPath, URI decryptedPath);
}
