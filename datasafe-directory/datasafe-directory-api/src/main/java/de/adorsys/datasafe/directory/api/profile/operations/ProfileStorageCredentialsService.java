package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

import java.util.Set;

/**
 * Defines storage credentials management operations.
 */
public interface ProfileStorageCredentialsService {

    /**
     * Registers credentials that allows user to access remote filesystems (i.e. Amazon S3 bucket)
     * @param user Owner of storage credentials
     * @param storageId Storage identifier - will be used to match URI in access request to storage credentials
     * @param credentials Access credentials for storage.
     */
    void registerStorageCredentials(UserIDAuth user, StorageIdentifier storageId, StorageCredentials credentials);

    /**
     * Removes storeds credentials that allows user to access remote filesystems (i.e. Amazon S3 bucket)
     * @param user Owner of storage credentials
     * @param storageId Storage identifier - will be used to match URI in access request to storage credentials
     */
    void deregisterStorageCredentials(UserIDAuth user, StorageIdentifier storageId);

    /**
     * Lists storage credentials identifiers (regex-mappings)
     * @param user storage credentials owner
     */
    Set<StorageIdentifier> listRegisteredStorageCredentials(UserIDAuth user);
}
