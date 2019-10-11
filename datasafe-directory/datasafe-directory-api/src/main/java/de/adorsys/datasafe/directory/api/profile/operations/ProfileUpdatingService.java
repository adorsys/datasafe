package de.adorsys.datasafe.directory.api.profile.operations;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;

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
}
