package de.adorsys.datasafe.directory.api.profile.dfs;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;

/**
 * Service that performs final resource location resolution and credentials binding.
 * Runs just before accessing underlying filesystem.
 * May perform additional resource resolution and enriches resource location with credentials necessary
 * to access underlying filesystem.
 * For example it can act as gateway transforming dfs://user/path into s3://user-1-bucket/root/path
 * Or it can add credentials transforming:
 * s3://user-1-bucket/root/path into s3://username:password@user-1-bucket/root/path
 */
public interface BucketAccessService {

    /**
     * Gets credentials to access specified private resource location and may perform additional path resolution.
     * @param user Requesting user
     * @param resource User-internal resource (private, either relative or absolute)
     * @return Physical resource location with credentials required to access it.
     */
    AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, PrivateResource resource);

    /**
     * Gets credentials to access specified public resource location and may perform additional path resolution.
     * @param user Requesting user
     * @param resource User-external resource (shareable, either relative or absolute)
     * @return Physical resource location with credentials required to access it.
     */
    AbsoluteLocation<PublicResource> publicAccessFor(UserID user, PublicResource resource);

    /**
     * Gets credentials to access specified private resource as a system and may perform additional path resolution.
     * This is mostly for storing user metadata like his profile.
     * <b>Note</b> that system <b>should not</b> have access to real user private/public resource.
     * @param resource System-internal resource (private, either relative or absolute)
     * @return Physical resource location with credentials required to access it.
     */
    AbsoluteLocation withSystemAccess(AbsoluteLocation resource);
}
