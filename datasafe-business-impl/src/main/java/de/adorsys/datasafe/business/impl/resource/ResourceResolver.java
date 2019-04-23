package de.adorsys.datasafe.business.impl.resource;

import de.adorsys.datasafe.business.api.directory.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ResourceResolver {

    private final ProfileRetrievalService profile;

    @Inject
    public ResourceResolver(ProfileRetrievalService profile) {
        this.profile = profile;
    }

    public PublicResource resolveRelativeToPublicInbox(UserID userID, PublicResource resource) {
        return resolveRelative(
                resource,
                () -> profile.publicProfile(userID).getInbox()
        );
    }

    public PrivateResource resolveRelativeToPrivateInbox(UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getInboxWithWriteAccess()
        );
    }

    public PrivateResource resolveRelativeToPrivate(UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getPrivateStorage()
        );
    }

    private  <T extends ResourceLocation<T>> T resolveRelative(T resource, Supplier<ResourceLocation<T>> resolveTo) {
        if (resource.locationWithAccess().isAbsolute()) {
            return resource;
        }

        return resolveTo.get().resolve(resource);
    }
}
