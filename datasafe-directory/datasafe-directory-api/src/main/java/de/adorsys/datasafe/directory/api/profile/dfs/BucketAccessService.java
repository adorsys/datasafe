package de.adorsys.datasafe.directory.api.profile.dfs;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;

/**
 * Service that performs final resource location resolution and credentials binding.
 * Runs just before accessing underlying filesystem.
 * May perform additional resource resolution and enriches resource location with credentials necessary
 * to access underlying filesystem.
 */
public interface BucketAccessService {

    /**
     * Gets credentials to access specified private resource location and may perform additional path resolution.
     * @param user Requesting user
     * @param resource User-internal resource (private)
     * @return Physical resource location with credentials required to access it.
     */
    AbsoluteLocation<PrivateResource> privateAccessFor(UserIDAuth user, ResourceLocation resource);

    /**
     * Gets credentials to access specified public resource location and may perform additional path resolution.
     * @param user Requesting user
     * @param resource User-external resource (shareable)
     * @return Physical resource location with credentials required to access it.
     */
    AbsoluteLocation<PublicResource> publicAccessFor(UserID user, ResourceLocation resource);
}
