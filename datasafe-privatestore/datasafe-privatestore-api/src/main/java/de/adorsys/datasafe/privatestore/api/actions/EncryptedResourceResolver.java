package de.adorsys.datasafe.privatestore.api.actions;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

/**
 * Resolves logical resource location into encrypted absolute location and vice-versa. For example, when
 * user requests some/path/to/file this resolver will provide physical location of this resource by
 * encrypting its relative path (i.e. some/path/to/file -> encryptedSome/encryptedPath/encryptedTo/encryptedFile)
 * and resolving it against user private files folder, so that some/path/to/file will be converted to i.e.
 * s3://bucket/user/privatespace/encryptedSome/encryptedPath/encryptedTo/encryptedFile
 */
public interface EncryptedResourceResolver {

    /**
     * Encrypts relative resource location and resolves it against user private files. For example
     * some/path/to/file transforms to s3://bucket/user/privatespace/encryptedSome/encryptedPath/encryptedTo/encryptedFile
     * @param auth User authorization
     * @param resource Relative resource location
     * @return Encrypted relative resource location
     */
    AbsoluteLocation<PrivateResource> encryptAndResolvePath(UserIDAuth auth, PrivateResource resource);

    /**
     * Decrypts resource location (relative or absolute) and resolves it against user private files. For example
     * s3://bucket/user/privatespace/encryptedSome/encryptedPath/encryptedTo/encryptedFile transforms to some/path/to/file
     * encryptedSome/encryptedPath/encryptedTo/encryptedFile transforms to some/path/to/file
     * @param auth User authorization
     * @param resource Relative or absolute resource location
     * @return Encrypted relative resource location
     */
    AbsoluteLocation<PrivateResource> decryptAndResolvePath(
            UserIDAuth auth, PrivateResource resource, PrivateResource root
    );
}
