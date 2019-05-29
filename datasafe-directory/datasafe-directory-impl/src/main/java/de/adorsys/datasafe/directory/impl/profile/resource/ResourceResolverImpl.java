package de.adorsys.datasafe.directory.impl.profile.resource;

import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;

import javax.inject.Inject;
import java.util.function.Supplier;

public class ResourceResolverImpl implements ResourceResolver {

    private final ProfileRetrievalService profile;

    @Inject
    public ResourceResolverImpl(ProfileRetrievalService profile) {
        this.profile = profile;
    }

    @Override
    public AbsoluteLocation<PublicResource> resolveRelativeToPublicInbox(
            UserID userID, PublicResource resource) {
        return resolveRelative(
                resource,
                () -> profile.publicProfile(userID).getInbox()
        );
    }

    @Override
    public AbsoluteLocation<PrivateResource> resolveRelativeToPrivateInbox(
            UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getInboxWithFullAccess()
        );
    }

    @Override
    public AbsoluteLocation<PrivateResource> resolveRelativeToPrivate(
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

    private  <T extends ResourceLocation<T>> AbsoluteLocation<T> resolveRelative(
            T resource, Supplier<ResourceLocation<T>> resolveTo) {
        if (isAbsolute(resource)) {
            return new AbsoluteLocation<>(resource);
        }

        return new AbsoluteLocation<>(resource.resolveFrom(resolveTo.get()));
    }
}
