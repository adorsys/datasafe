package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.utils.Log;
import lombok.*;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hardened URI class that prevents leaking of URI content.
 */
@EqualsAndHashCode(of = "wrapped")
@RequiredArgsConstructor
public class Uri {

    @NonNull
    @Getter
    private final URI wrapped;

    @SneakyThrows
    public Uri(String path) {
        try {
            this.wrapped = new URI(path);
        } catch (URISyntaxException ex) {
            throw new URISyntaxException(Log.secure(ex.getInput(), "/"), ex.getReason());
        }
    }

    /**
     * {@link URI#getPath()}
     */
    public String getPath() {
        return wrapped.getPath();
    }

    /**
     * {@link URI#resolve(String)}
     */
    public Uri resolve(String uri) {
        return new Uri(this.wrapped.resolve(uri));
    }

    /**
     * {@link URI#resolve(URI)}
     */
    public Uri resolve(URI uri) {
        return resolve(new Uri(uri));
    }

    /**
     * Same as {@link URI#resolve(URI)}, just wrapped result
     */
    public Uri resolve(Uri uri) {
        return new Uri(this.wrapped.resolve(uri.getWrapped()));
    }

    /**
     * Same as {@link URI#relativize(URI)}, just wrapped result
     */
    public Uri relativize(Uri uri) {
        return new Uri(this.wrapped.relativize(uri.getWrapped()));
    }

    /**
     * @return Makes directory from underlying resource
     * I.e. http://example.com/foo becomes http://example.com/foo/ so it can be resolved against
     */
    public Uri asDir() {
        return isDir() ? this : new Uri(this.wrapped.toASCIIString() + "/");
    }

    /**
     * {@link URI#toASCIIString()}
     */
    public String toASCIIString() {
        return this.wrapped.toASCIIString();
    }

    /**
     * {@link URI#isAbsolute()}
     */
    public boolean isAbsolute() {
        return this.wrapped.isAbsolute();
    }

    /**
     * @return If wrapped resource has bytes inside
     */
    public boolean isEmpty() {
        return this.wrapped.toASCIIString().isEmpty();
    }

    /**
     * @return If this resource can act as path root
     * I.e. "http://example.com/foo".resolve("bar") -> http://example.com/bar - it can't be path root
     * "http://example.com/foo/".resolve("bar") -> http://example.com/foo/bar - it is path root
     */
    public boolean isDir() {
        return this.wrapped.toASCIIString().endsWith("/");
    }

    /**
     * @return wrapped resource
     */
    public URI asURI() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "Uri{" +
                "uri=" + Log.secure(wrapped) +
                '}';
    }
}
