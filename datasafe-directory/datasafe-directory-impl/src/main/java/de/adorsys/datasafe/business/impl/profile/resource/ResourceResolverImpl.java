package de.adorsys.datasafe.business.impl.profile.resource;

import de.adorsys.datasafe.business.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.version.types.UserID;
import de.adorsys.datasafe.business.api.version.types.UserIDAuth;
import de.adorsys.datasafe.business.api.version.types.resource.AbsoluteResourceLocation;
import de.adorsys.datasafe.business.api.version.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.version.types.resource.PublicResource;
import de.adorsys.datasafe.business.api.version.types.resource.ResourceLocation;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ResourceResolverImpl implements ResourceResolver {

    private final ProfileRetrievalService profile;

    @Inject
    public ResourceResolverImpl(ProfileRetrievalService profile) {
        this.profile = profile;
    }

    @Override
    public AbsoluteResourceLocation<PublicResource> resolveRelativeToPublicInbox(
            UserID userID, PublicResource resource) {
        return resolveRelative(
                resource,
                () -> profile.publicProfile(userID).getInbox()
        );
    }

    @Override
    public AbsoluteResourceLocation<PrivateResource> resolveRelativeToPrivateInbox(
            UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getInboxWithWriteAccess()
        );
    }

    @Override
    public AbsoluteResourceLocation<PrivateResource> resolveRelativeToPrivate(
            UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getPrivateStorage()
        );
    }

    @Override
    public  <T extends ResourceLocation<T>> boolean isAbsolute(T resource) {
        return resource.location().isAbsolute();
    }

    private  <T extends ResourceLocation<T>> AbsoluteResourceLocation<T> resolveRelative(
            T resource, Supplier<ResourceLocation<T>> resolveTo) {
        if (isAbsolute(resource)) {
            return new AbsoluteResourceLocation<>(resource);
        }

        return new AbsoluteResourceLocation<>(resource.resolve(resolveTo.get()));
    }
}
