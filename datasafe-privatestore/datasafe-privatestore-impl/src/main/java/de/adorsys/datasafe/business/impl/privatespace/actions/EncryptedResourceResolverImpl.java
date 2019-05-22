package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.net.URI;

public class EncryptedResourceResolverImpl implements EncryptedResourceResolver {

    private final ResourceResolver resolver;
    private final PathEncryption pathEncryption;

    @Inject
    public EncryptedResourceResolverImpl(ResourceResolver resolver, PathEncryption pathEncryption) {
        this.resolver = resolver;
        this.pathEncryption = pathEncryption;
    }

    @Override
    public PrivateResource encrypt(UserIDAuth auth, PrivateResource resource) {
        URI decryptedPath = resource.location();
        URI encryptedRelativePath = pathEncryption.encrypt(auth, decryptedPath);

        return resource.resolve(encryptedRelativePath, decryptedPath);
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
            URI encryptedPath = resource.location();
            URI decryptedPath = pathEncryption.decrypt(auth, encryptedPath);

            return new AbsoluteLocation<>(
                    resolver.resolveRelativeToPrivate(auth, resource).getResource().resolve(
                            encryptedPath,
                            decryptedPath)
            );
        }

        URI encryptedPath;
        URI relative = relativize(root.location(), resource.location());
        if (!root.encryptedPath().toString().endsWith("/") && !relative.toString().isEmpty()) {
            encryptedPath = URI.create(root.encryptedPath().toString() + "/")
                    .resolve(relativize(root.location(), resource.location()));
        } else {
            encryptedPath = root.encryptedPath().resolve(relative);
        }

        URI decryptedPath = pathEncryption.decrypt(auth, encryptedPath);

        return new AbsoluteLocation<>(
                resolver.resolveRelativeToPrivate(auth, resource).getResource().resolve(encryptedPath, decryptedPath)
        );
    }


    private static URI relativize(URI root, URI resource) {
        if (root.isAbsolute()) {
            return root.relativize(resource);
        }

        String rootString = root.toASCIIString();
        String resourceString = resource.toASCIIString();

        String relative = resourceString.substring(resourceString.indexOf(rootString) + rootString.length());
        return URI.create(relative);
    }
}
