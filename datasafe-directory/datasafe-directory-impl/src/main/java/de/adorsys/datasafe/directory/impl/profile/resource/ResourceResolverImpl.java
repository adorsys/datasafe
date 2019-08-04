package de.adorsys.datasafe.directory.impl.profile.resource;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRetrievalService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Default resource resolver that simply resolves users' internal resource location into absolute
 * resources by prepending proper resource root.
 */
@RuntimeDelegate
public class ResourceResolverImpl implements ResourceResolver {

    private final ProfileRetrievalService profile;
    private final BucketAccessService bucketAccessService;

    @Inject
    public ResourceResolverImpl(ProfileRetrievalService profile, BucketAccessService bucketAccessService) {
        this.profile = profile;
        this.bucketAccessService = bucketAccessService;
    }

    /**
     * @return Prepends INBOX location before {@code resource}.
     */
    @Override
    public AbsoluteLocation<PublicResource> resolveRelativeToPublicInbox(
        UserID userID, PublicResource resource) {

        return bucketAccessService.publicAccessFor(
            userID,
            resolveRelative(resource, () -> profile.publicProfile(userID).getInbox())
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

        return bucketAccessService.privateAccessFor(
            userID,
            resolveRelative(resource, () -> profile.privateProfile(userID).getInboxWithFullAccess())
        );
    }

    /**
     * @return Prepends privatespace location before {@code resource}.
     */
    @Override
    public AbsoluteLocation<PrivateResource> resolveRelativeToPrivate(
        UserIDAuth userID, PrivateResource resource, StorageIdentifier identifier) {

        return bucketAccessService.privateAccessFor(
            userID,
            resolveRelative(resource, () -> profile.privateProfile(userID).getPrivateStorage().get(identifier))
        );
    }

    /**
     * Simply calls absolute check on location.
     */
    @Override
    public <T extends ResourceLocation<T>> boolean isAbsolute(T resource) {
        return resource.location().isAbsolute();
    }

    private <T extends ResourceLocation<T>> T resolveRelative(
        T resource, Supplier<ResourceLocation<T>> resolveTo) {
        if (isAbsolute(resource)) {
            return resource;
        }

        return resource.resolveFrom(resolveTo.get());
    }
}
