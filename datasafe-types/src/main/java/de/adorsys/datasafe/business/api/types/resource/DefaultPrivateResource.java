package de.adorsys.datasafe.business.api.types.resource;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.net.URI;

@ToString
@RequiredArgsConstructor
public final class DefaultPrivateResource implements PrivateResource {

    private static final URI URI_ROOT = URI.create("./");
    private static final URI EMPTY_URI = URI.create("");

    public static final DefaultPrivateResource ROOT = new DefaultPrivateResource();

    private final URI container;
    private final URI encryptedPath;
    private final URI decryptedPath;

    private DefaultPrivateResource() {
        this.container = URI_ROOT;
        this.decryptedPath = EMPTY_URI;
        this.encryptedPath = EMPTY_URI;
    }

    // TODO: Hide it
    public DefaultPrivateResource(URI containerUri) {
        this.container = containerUri;
        this.decryptedPath = EMPTY_URI;
        this.encryptedPath = EMPTY_URI;
    }

    public static PrivateResource forPrivate(URI path) {
        return new DefaultPrivateResource().resolve(path, EMPTY_URI);
    }

    @Override
    public URI encryptedPath() {
        return encryptedPath;
    }

    @Override
    public URI decryptedPath() {
        return decryptedPath;
    }

    @Override
    public PrivateResource resolve(URI encryptedPath, URI decryptedPath) {
        if (encryptedPath.isAbsolute()) {
            throw new IllegalArgumentException("Encrypted path must be relative");
        }

        if (decryptedPath.isAbsolute()) {
            throw new IllegalArgumentException("Decrypted path must be relative");
        }

        return new DefaultPrivateResource(resolveContainer(container, encryptedPath), encryptedPath, decryptedPath);
    }

    @Override
    public URI location() {
        if (encryptedPath.toString().isEmpty()) {
            return container;
        }

        return container.resolve(encryptedPath);
    }

    @Override
    public PrivateResource applyRoot(ResourceLocation absolute) {
        if (!container.isAbsolute()) {
            return new DefaultPrivateResource(
                    absolute.location().resolve(container), encryptedPath, decryptedPath
            );
        }
        return new DefaultPrivateResource(absolute.location(), encryptedPath, decryptedPath);
    }

    private static URI resolveContainer(URI root, URI encryptedPath) {
        String pathStr = encryptedPath.toASCIIString();
        if (pathStr.isEmpty()) {
            return root;
        }

        String rootStr = root.toASCIIString();
        int pos = rootStr.indexOf(pathStr);

        if (pos <= 0) {
            return root;
        }

        return URI.create(rootStr.substring(0, pos));
    }
}
