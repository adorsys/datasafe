package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

/**
 * Updates users' profile in a system.
 */
public interface ProfileUpdatingService {

    /**
     * Updates users' public profile
     * @param forUser User to update profile
     * @param profile New users' profile
     */
    void updatePublicProfile(UserIDAuth forUser, UserPublicProfile profile);

    /**
     * Updates users' private profile
     * @param forUser User to update profile
     * @param profile New users' profile
     */
    void updatePrivateProfile(UserIDAuth forUser, UserPrivateProfile profile);

    /**
     * Updates user's keystore password.
     * @param forUser user, whose keystore to update
     * @param newPassword new ReadKeyPassword for a user
     * NOTE: (For S3-like storage) Due to S3 eventual consistency some requests using old passwords will succeed,
     * some requests using new password will fail until storage propagates data.
     */
    void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword);

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
}
