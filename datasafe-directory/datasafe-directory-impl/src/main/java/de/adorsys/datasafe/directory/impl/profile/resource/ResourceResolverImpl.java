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

/**
 * Default resource resolver that simply resolves users' internal resource location into absolute
 * resources by prepending proper resource root.
 */
public class ResourceResolverImpl implements ResourceResolver {

    private final ProfileRetrievalService profile;

    @Inject
    public ResourceResolverImpl(ProfileRetrievalService profile) {
        this.profile = profile;
    }

    /**
     * @return Prepends INBOX location before {@code resource}.
     */
    @Override
    public AbsoluteLocation<PublicResource> resolveRelativeToPublicInbox(
            UserID userID, PublicResource resource) {
        return resolveRelative(
                resource,
                () -> profile.publicProfile(userID).getInbox()
        );
    }

    /**
     * @return Prepends INBOX-private location before {@code resource}
     * @implNote typically INBOX-private location is not different from INBOX, it might simply have extra
     * credentials
     */
    @Override
    public AbsoluteLocation<PrivateResource> resolveRelativeToPrivateInbox(
            UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getInboxWithFullAccess()
        );
    }

    /**
     * @return Prepends privatespace location before {@code resource}.
     */
    @Override
    public AbsoluteLocation<PrivateResource> resolveRelativeToPrivate(
            UserIDAuth userID, PrivateResource resource) {
        return resolveRelative(
                resource,
                () -> profile.privateProfile(userID).getPrivateStorage()
        );
    }

    /**
     * Simply calls absolute check on location.
     */
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
