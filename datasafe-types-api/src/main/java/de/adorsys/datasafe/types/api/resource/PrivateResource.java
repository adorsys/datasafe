package de.adorsys.datasafe.types.api.resource;

import java.net.URI;

/**
 * The interface that represents some private resource location relative to some container (resource root).
 * Private resource has an encrypted physical path so that no one can know its path and name when looking
 * at its physical location.
 * To achieve that it has {@code encryptedPath} that identifies its location on physical storage and its
 * decrypted representation - {@code decryptedPath}.
 */
public interface PrivateResource extends ResourceLocation<PrivateResource> {

    /**
     * Physical path for a document
     * @return encrypted relative URI (relative to container)
     */
    URI encryptedPath();

    /**
     * Logical path for a document
     * @return decrypted relative URI (relative to container)
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
