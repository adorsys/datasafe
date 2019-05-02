package de.adorsys.datasafe.business.impl.privatespace.actions;

import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.resource.ResourceResolver;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import javax.inject.Inject;
import java.net.URI;

public class EncryptedResourceResolver {

    private final ResourceResolver resolver;
    private final PathEncryption pathEncryption;

    @Inject
    public EncryptedResourceResolver(ResourceResolver resolver, PathEncryption pathEncryption) {
        this.resolver = resolver;
        this.pathEncryption = pathEncryption;
    }

    public PrivateResource encryptAndResolvePath(UserIDAuth auth, PrivateResource resource) {
        // TODO: multi-DFS check
        if (resolver.isAbsolute(resource)) {
            return resource;
        }

        URI decryptedPath = resource.location();
        URI encryptedRelativePath = pathEncryption.encrypt(auth, decryptedPath);

        return resolver.resolveRelativeToPrivate(auth, resource.resolve(encryptedRelativePath, decryptedPath));
    }

    public PrivateResource decryptAndResolvePath(UserIDAuth auth, PrivateResource resource, PrivateResource root) {
        // TODO: multi-DFS check
        if (!resolver.isAbsolute(resource)) {
            URI encryptedPath = resource.location();
            URI decryptedPath = pathEncryption.decrypt(auth, encryptedPath);
            return resolver.resolveRelativeToPrivate(auth, resource).resolve(encryptedPath, decryptedPath);
        }

        URI encryptedPath = relativize(root.location(), resource.location());
        URI decryptedPath = pathEncryption.decrypt(auth, encryptedPath);

        return resolver.resolveRelativeToPrivate(auth, resource).resolve(encryptedPath, decryptedPath);
    }


    private static URI relativize(URI root, URI resource) {
        if (root.isAbsolute()) {
            return root.relativize(resource);
        }

        String rootString = root.toASCIIString();
        String resourceString = resource.toASCIIString();

        return URI.create(resourceString.substring(resourceString.indexOf(rootString) + rootString.length()));
    }

}
