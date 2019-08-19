package de.adorsys.datasafe.directory.api.config;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

/**
 * Default configuration for the case when user profiles are located on some DFS.
 */
public interface DFSConfig {

    /**
     * Get credentials to read key in users' keystore.
     */
    KeyStoreAuth privateKeyStoreAuth(UserIDAuth auth);

    /**
     * Location of users public profile (where is the file with his public profile).
     */
    AbsoluteLocation publicProfile(UserID forUser);

    /**
     * Location of users private profiles (where is the file with his private profile).
     */
    AbsoluteLocation privateProfile(UserID forUser);

    /**
     * @param id User authentication
     * @return Default template for private user profile
     */
    CreateUserPrivateProfile defaultPrivateTemplate(UserIDAuth id);

    /**
     * @param id User authentication
     * @return Default template for public user profile
     */
    CreateUserPublicProfile defaultPublicTemplate(UserID id);
}
