package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;

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
     * Also creates his keystore if it does not exist at specified location and in that case publishes
     * users' public keys into location specified by users' public profile
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param profile Private profile part
     */
    void registerPrivate(CreateUserPrivateProfile profile);

    /**
     * Register user using all-default values.
     * IMPORTANT: For cases when user profile is stored on S3 without object locks, this requires some global
     * synchronization due to eventual consistency or you need to supply globally unique username on registration.
     * @param user User authorization to register
     */
    void registerUsingDefaults(UserIDAuth user);
}
