package de.adorsys.datasafe.types.api.resource;

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
    Uri encryptedPath();

    /**
     * Logical path for a document
     * @return decrypted relative URI (relative to container)
     */
    Uri decryptedPath();

    /**
     * Rebases/resolves decrypted/encrypted path against new resource root.
     * @param encryptedPath encrypted URI value, should be used to construct absolute URI.
     * @param decryptedPath decrypted URI value, used for easy navigation
     * @return new private resource that points to resource identified by encryptedPath
     * When calling "s3://bucket/".resolve(file/root, aaa/bbb) result will be located at s3://bucket/aaa/bbb
     */
    PrivateResource resolve(Uri encryptedPath, Uri decryptedPath);

    PrivateResource withAuthority(String username, String password);
}
