package de.adorsys.datasafe.types.api.resource;

import lombok.RequiredArgsConstructor;

import java.net.URI;

/**
 * Basic class that represents some private resource path. This private resource path has 3 components:
 * 1. container - some unencrypted path that identifies root location (example: s3 bucket URI)
 * 2. encryptedPath - encrypted path of absolute resource location, so that container + encryptedPath is absolute
 * resource location
 * 3. decryptedPath - decrypted representation of encryptedPath component.
 */
@RequiredArgsConstructor
public class BasePrivateResource implements PrivateResource {

    private static final Uri URI_ROOT = new Uri("./");
    private static final Uri EMPTY_URI = new Uri("");

    private final Uri container;
    private final Uri encryptedPath;
    private final Uri decryptedPath;

    private BasePrivateResource() {
        this.container = URI_ROOT;
        this.decryptedPath = EMPTY_URI;
        this.encryptedPath = EMPTY_URI;
    }

    // TODO: Hide it
    public BasePrivateResource(Uri containerUri) {
        this.container = containerUri;
        this.decryptedPath = EMPTY_URI;
        this.encryptedPath = EMPTY_URI;
    }

    public static PrivateResource forPrivate(String path) {
        return forPrivate(new Uri(path));
    }

    public static PrivateResource forPrivate(URI path) {
        return forPrivate(new Uri(path));
    }

    public static PrivateResource forPrivate(Uri path) {
        if (path.isAbsolute()) {
            return new BasePrivateResource(path).resolve(EMPTY_URI, EMPTY_URI);
        }

        return new BasePrivateResource().resolve(path, EMPTY_URI);
    }

    public static AbsoluteLocation<PrivateResource> forAbsolutePrivate(String path) {
        return forAbsolutePrivate(new Uri(path));
    }

    public static AbsoluteLocation<PrivateResource> forAbsolutePrivate(URI path) {
        return forAbsolutePrivate(new Uri(path));
    }

    public static AbsoluteLocation<PrivateResource> forAbsolutePrivate(Uri path) {
        return new AbsoluteLocation<>(new BasePrivateResource(path).resolve(EMPTY_URI, EMPTY_URI));
    }

    @Override
    public Uri encryptedPath() {
        return encryptedPath;
    }

    @Override
    public Uri decryptedPath() {
        return decryptedPath;
    }

    @Override
    public PrivateResource resolve(Uri encryptedPath, Uri decryptedPath) {
        if (encryptedPath.isAbsolute()) {
            throw new IllegalArgumentException("Encrypted path must be relative");
        }

        if (decryptedPath.isAbsolute()) {
            throw new IllegalArgumentException("Decrypted path must be relative");
        }

        return new BasePrivateResource(resolveContainer(container, encryptedPath), encryptedPath, decryptedPath);
    }

    @Override
    public Uri location() {
        if (encryptedPath.isEmpty()) {
            return container;
        }

        return container.resolve(encryptedPath);
    }

    @Override
    public PrivateResource resolveFrom(ResourceLocation absolute) {
        if (!container.isAbsolute()) {
            Uri absoluteUri = absolute.location();
            if (!absoluteUri.isDir()) {
                absoluteUri = absoluteUri.asDir();
            }

            return new BasePrivateResource(
                    absoluteUri.resolve(container), encryptedPath, decryptedPath
            );
        }
        return new BasePrivateResource(absolute.location(), encryptedPath, decryptedPath);
    }

    @Override
    public PrivateResource withAuthority(String username, String password) {
        return new BasePrivateResource(container.withAuthority(username, password), encryptedPath, decryptedPath);
    }

    @Override
    public String toString() {
        return "BasePrivateResource{" +
                "container=" + container +
                ", encryptedPath=" + encryptedPath +
                ", decryptedPath=" + decryptedPath +
                '}';
    }

    private static Uri resolveContainer(Uri root, Uri encryptedPath) {
        String pathStr = encryptedPath.asString();

        if (pathStr.contains("/")) {
            pathStr = pathStr.split("/", 2)[0];
        }

        if (pathStr.isEmpty()) {
            return root;
        }

        String rootStr = root.asString();
        int pos = rootStr.indexOf(pathStr);

        if (pos <= 0) {
            return root;
        }

        return new Uri(URI.create(rootStr.substring(0, pos)));
    }
}
