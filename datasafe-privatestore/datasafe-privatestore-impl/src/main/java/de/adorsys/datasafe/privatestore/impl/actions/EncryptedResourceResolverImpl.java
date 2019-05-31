package de.adorsys.datasafe.privatestore.impl.actions;

import de.adorsys.datasafe.directory.api.resource.ResourceResolver;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.privatestore.api.actions.EncryptedResourceResolver;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;

import javax.inject.Inject;

/**
 * Default encrypted resource resolver that delegates the task of encrypting/decrypting path to
 * {@link PathEncryption} and resolves relative paths using {@link PathEncryption}, also decrypts
 * absolute paths by relativizing path against users' privatespace directory and decrypting it.
 */
public class EncryptedResourceResolverImpl implements EncryptedResourceResolver {

    private final ResourceResolver resolver;
    private final PathEncryption pathEncryption;

    @Inject
    public EncryptedResourceResolverImpl(ResourceResolver resolver, PathEncryption pathEncryption) {
        this.resolver = resolver;
        this.pathEncryption = pathEncryption;
    }

    @Override
    public AbsoluteLocation<PrivateResource> encryptAndResolvePath(UserIDAuth auth, PrivateResource resource) {
        if (resolver.isAbsolute(resource)) {
            return new AbsoluteLocation<>(resource);
        }

        return resolver.resolveRelativeToPrivate(auth, encrypt(auth, resource));
    }

    @Override
    public AbsoluteLocation<PrivateResource> decryptAndResolvePath(
        UserIDAuth auth, PrivateResource resource, PrivateResource root) {
        if (!resolver.isAbsolute(resource)) {
            Uri encryptedPath = resource.location();
            Uri decryptedPath = pathEncryption.decrypt(auth, encryptedPath);

            return new AbsoluteLocation<>(
                resolver.resolveRelativeToPrivate(auth, resource).getResource().resolve(
                    encryptedPath,
                    decryptedPath)
            );
        }

        Uri relative = relativize(root.location(), resource.location());

        Uri encryptedPath = computeEncryptedPath(root, relative);
        Uri decryptedPath = pathEncryption.decrypt(auth, encryptedPath);

        return new AbsoluteLocation<>(
            resolver.resolveRelativeToPrivate(auth, resource).getResource().resolve(encryptedPath, decryptedPath)
        );
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

        String rootString = root.toASCIIString();
        String resourceString = resource.toASCIIString();

        String relative = resourceString.substring(resourceString.indexOf(rootString) + rootString.length());
        return new Uri(relative);
    }
}
