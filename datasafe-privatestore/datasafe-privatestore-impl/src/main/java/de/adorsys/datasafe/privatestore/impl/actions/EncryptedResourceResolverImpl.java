package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;

import javax.inject.Inject;
import java.net.URI;
import java.util.function.Function;

/**
 * Default encrypted resource resolver that delegates the task of encrypting/decrypting path to
 * {@link PathEncryption} and resolves relative paths using {@link PathEncryption}, also decrypts
 * absolute paths by relativizing path against users' privatespace directory and decrypting it.
 */
@RuntimeDelegate
public class EncryptedResourceResolverImpl implements EncryptedResourceResolver {

    private final BucketAccessService bucketAccessService;
    private final ResourceResolver resolver;
    private final PathEncryption pathEncryption;

    @Inject
    public EncryptedResourceResolverImpl(BucketAccessService bucketAccessService, ResourceResolver resolver,
                                         PathEncryption pathEncryption) {
        this.bucketAccessService = bucketAccessService;
        this.resolver = resolver;
        this.pathEncryption = pathEncryption;
    }

    @Override
    public AbsoluteLocation<PrivateResource> encryptAndResolvePath(UserIDAuth auth, PrivateResource resource,
                                                                   StorageIdentifier identifier) {
        if (resolver.isAbsolute(resource)) {
            return bucketAccessService.privateAccessFor(auth, resource);
        }

        return resolver.resolveRelativeToPrivate(auth, encrypt(auth, resource), identifier);
    }

    @Override
    public Function<PrivateResource, AbsoluteLocation<PrivateResource>> decryptingResolver(
            UserIDAuth auth, PrivateResource root, StorageIdentifier identifier) {
        Function<Uri, Uri> decryptor = pathEncryption.decryptor(auth);

        return resource -> {
            Uri encryptedPart = computeEncryptedPart(root, resource);
            Uri decryptedPart = decryptor.apply(encryptedPart);
            return new AbsoluteLocation<>(
                    resolver.resolveRelativeToPrivate(auth, resource, identifier)
                        .getResource().resolve(encryptedPart, decryptedPart)
            );
        };
    }

    private Uri computeEncryptedPart(PrivateResource root, PrivateResource resource) {
        Uri relative = relativize(root.location(), resource.location());
        return computeEncryptedPath(root, relative);
    }

    private PrivateResource encrypt(UserIDAuth auth, PrivateResource resource) {
        Uri decryptedPath = resource.location();
        Uri encryptedRelativePath = pathEncryption.encrypt(auth, decryptedPath);

        return resource.resolve(encryptedRelativePath, decryptedPath);
    }

    private Uri computeEncryptedPath(PrivateResource root, Uri relative) {
        if (hasRelativePath(relative)) {
            return handleResourceRelativeToRoot(root, relative);
        }

        return handleEmptyRelative(root);
    }

    private boolean hasRelativePath(Uri relative) {
        return !relative.isEmpty();
    }

    private Uri handleEmptyRelative(PrivateResource root) {
        return root.encryptedPath();
    }

    private Uri handleResourceRelativeToRoot(PrivateResource root, Uri relative) {
        if (root.encryptedPath().isEmpty()) {
            return relative;
        }

        return root.encryptedPath().asDir().resolve(relative);
    }

    private static Uri relativize(Uri root, Uri resource) {
        if (root.isAbsolute()) {
            return root.relativize(resource);
        }

        String rootString = root.asString();
        String resourceString = resource.asString();

        String relative = resourceString.substring(resourceString.indexOf(rootString) + rootString.length());
        return new Uri(URI.create(relative));
    }
}
