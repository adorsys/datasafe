package de.adorsys.datasafe.business.impl.profile.resource;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ResourceResolverImpl implements ResourceResolver {

    private final ProfileRetrievalService profile;

    @Inject
    public ResourceResolverImpl(ProfileRetrievalService profile) {
        this.profile = profile;
    }

    @Override
    public PublicResource resolveRelativeToPublicInbox(UserID userID, PublicResource resource) {
        return resolveRelative(
                resource,
                () -> profile.publicProfile(userID).getInbox()
        );
    }

    @Override
    public PrivateResource resolveRelativeToPrivateInbox(UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getInboxWithWriteAccess()
        );
    }

    @Override
    public PrivateResource resolveRelativeToPrivate(UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getPrivateStorage()
        );
    }

    @Override
    public  <T extends ResourceLocation<T>> boolean isAbsolute(T resource) {
        return resource.location().isAbsolute();
    }

    private  <T extends ResourceLocation<T>> T resolveRelative(T resource, Supplier<ResourceLocation<T>> resolveTo) {
        if (isAbsolute(resource)) {
            return resource;
        }

        return resource.applyRoot(resolveTo.get());
    }
}
