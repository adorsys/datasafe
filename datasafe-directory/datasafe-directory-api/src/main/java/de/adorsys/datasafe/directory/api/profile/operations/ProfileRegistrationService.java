package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.*;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PublicResource;

/**
 * Registers user in system.
 */
public interface ProfileRegistrationService {

    /**
     * Registers public part of user's profile - his INBOX and where are his public keys.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param profile Public profile part
     */
    void registerPublic(CreateUserPublicProfile profile);

    /**
     * Updates user's keystore password.
     * @param forUser user, whose keystore to update
     * @param newPassword new ReadKeyPassword for a user
     * NOTE: (For S3-like storage) Due to S3 eventual consistency some requests using old passwords will succeed,
     * some requests using new password will fail until storage propagates data.
     */
    void updateReadKeyPassword(UserIDAuth forUser, ReadKeyPassword newPassword);

    /**
     * Registers private part of user profile - his keystore location, private folder location, etc.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param profile Private profile part
     */
    void registerPrivate(CreateUserPrivateProfile profile);

    /**
     * Creates keystore and publishes his public keys according to his
     * {@link de.adorsys.datasafe.directory.api.types.UserPrivateProfile}
     * @param user Keystore owner
     * @param profile Associate profile with this keystore
     * @param publishPubKeysTo Where to publish public keys associated with private keys in keystore
     */
    void createKeystore(UserIDAuth user, UserPrivateProfile profile, AbsoluteLocation<PublicResource> publishPubKeysTo);

    /**
     * Register user using all-default values.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param user User authorization to register
     */
    void registerUsingDefaults(UserIDAuth user);


    /**
     * Registers credentials that allows user to access remote filesystems (i.e. Amazon S3 bucket)
     * @param user Owner of storage credentials
     * @param storageId Storage identifier - will be used to match URI in access request to storage credentials
     * @param credentials Access credentials for storage.
     */
    void registerStorageCredentials(UserIDAuth user, StorageIdentifier storageId, StorageCredentials credentials);
}
