package de.adorsys.datasafe.directory.api.resource;

import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

/**
 * Resolves user internal (logical, relative) resources into absolute (physical) locations then can be directly accessed
 * using filesystem adapter.
 */
public interface ResourceResolver {

    /**
     * Resolves physical location of a given users' public resource (INBOX)
     * @param userID INBOX owner
     * @param resource location within INBOX
     * @return physical location of resource within users' INBOX
     */
    AbsoluteLocation<PublicResource> resolveRelativeToPublicInbox(UserID userID, PublicResource resource);

    /**
     * Provides full access to users' INBOX by viewing it as privatespace
     * @param userID INBOX as privatespace owner
     * @param resource location within INBOX
     * @return physical location of resource within users' INBOX seen as privatespace resource (with full control)
     */
    AbsoluteLocation<PrivateResource> resolveRelativeToPrivateInbox(UserIDAuth userID, PrivateResource resource);

    /**
     * Resolves physical location of a given users' private resource (privatespace)
     * @param userID Privatespace owner
     * @param resource location within privatespace
     * @return physical location of resource within users' privatespace
     */
    AbsoluteLocation<PrivateResource> resolveRelativeToPrivate(UserIDAuth userID, PrivateResource resource,
                                                               StorageIdentifier identifier);

    /**
     * Checks if a given resource location is absolute.
     * @param resource Resource location
     * @param <T> Generic
     * @return Is it absolute location?
     */
    <T extends ResourceLocation<T>> boolean isAbsolute(T resource);
}
