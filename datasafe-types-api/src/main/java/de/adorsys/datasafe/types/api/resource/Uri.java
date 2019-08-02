package de.adorsys.datasafe.types.api.resource;

import de.adorsys.datasafe.types.api.utils.Obfuscate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

/**
 * Hardened URI class that prevents leaking of URI content.
 * Note: Always prefer using functions that take URI as arguments.
 */
@EqualsAndHashCode(of = "wrapped")
public class Uri {

    @NonNull
    @Getter
    private final URI wrapped;

    public Uri(@NonNull URI wrapped) {
        this.wrapped = wrapped;
    }

    public Uri(String path) {
        this.wrapped = UriEncoderDecoder.encode(path);
    }

    /**
     * {@link URI#getPath()}
     * @return Path part of wrapped URI.
     */
    public String getPath() {
        return wrapped.getPath();
    }

    /**
     * {@link URI#getRawPath()}
     * @return Path part of wrapped URI.
     */
    public String getRawPath() {
        return wrapped.getRawPath();
    }

    /**
     * {@link URI#resolve(String)}
     * @param uri Path to resolve.
     * @return Uri that has <b>wrapped Uri + {@code uri}</b> as its path.
     */
    public Uri resolve(String uri) {
        return new Uri(this.wrapped.resolve(UriEncoderDecoder.encode(uri)));
    }

    /**
     * {@link URI#resolve(URI)}
     * @param uri Path to resolve.
     * @return Uri that has <b>wrapped Uri + {@code uri}</b> as its path.
     */
    public Uri resolve(URI uri) {
        return resolve(new Uri(uri));
    }

    /**
     * Same as {@link URI#resolve(URI)}, just wrapped result
     * @param uri Path to resolve.
     * @return Uri that has <b>wrapped Uri + {@code uri}</b> as its path.
     */
    public Uri resolve(Uri uri) {
        return new Uri(this.wrapped.resolve(uri.getWrapped()));
    }

    /**
     * Same as {@link URI#relativize(URI)}, just wrapped result
     * @param uri Path to relativize (relative or absolute).
     * @return Relative Uri that has {@code uri} without common part.
     */
    public Uri relativize(Uri uri) {
        return new Uri(this.wrapped.relativize(uri.getWrapped()));
    }

    /**
     * @return Makes file from underlying resource
     * For example, http://example.com/foo/ becomes http://example.com/foo.
     */
    public Uri asDir() {
        return isDir() ? this : new Uri(URI.create(this.wrapped.toString() + "/"));
    }

    /**
     * @return Makes directory from underlying resource
     * For example, http://example.com/foo becomes http://example.com/foo/ so it can be resolved against.
     */
    public Uri asFile() {
        return isDir() ? new Uri(URI.create(this.wrapped.toString().replaceAll("/$", ""))) : this;
    }

    /**
     * {@link URI#toASCIIString()}
     * @return ASCII representation of underlying URI.
     */
    public String toASCIIString() {
        return this.wrapped.toASCIIString();
    }

    /**
     * {@link URI#isAbsolute()}
     * @return If underlying URI is absolute (has host and protocol).
     */
    public boolean isAbsolute() {
        return this.wrapped.isAbsolute();
    }

    /**
     * @return If wrapped resource is not empty string.
     */
    public boolean isEmpty() {
        return this.wrapped.toString().isEmpty();
    }

    /**
     * @return If this resource can act as path root.
     * @implNote  For example "http://example.com/foo".resolve("bar") transforms to http://example.com/bar,
     * so it can't be path root.
     * "http://example.com/foo/".resolve("bar") transforms to http://example.com/foo/bar, so it is path root.
     */
    public boolean isDir() {
        return this.wrapped.toString().endsWith("/");
    }

    /**
     * @return wrapped resource
     */
    public URI asURI() {
        return wrapped;
    }

    /**
     * @return wrapped resource without authority
     */
    public URI withoutAuthority() {
        return URI.create(UriEncoderDecoder.withoutAuthority(wrapped));
    }

    /**
     * Returns human-friendly URL-decoded representation of this class, and strips authority
     * @return URL-decoded value (i.e. %20 will become ' ')
     */
    public String asString() {
        return UriEncoderDecoder.decodeAndDropAuthority(wrapped);
    }

    public Uri withAuthority(String username, String password) {
        if (!wrapped.isAbsolute()) {
            throw new IllegalStateException("Absolute resource is required to embed credentials");
        }

        String result = wrapped.getScheme();
        result += "://" + username + ":" + password + "@";
        result += wrapped.getHost();
        if (wrapped.getPort() != -1) {
            result += ":" + wrapped.getPort();
        }

        result += "/";

        if (!"/".equals(wrapped.getPath())) {
            result += wrapped.getPath().replaceAll("^/", "");
        }

        return new Uri(result);
    }

    @Override
    public String toString() {
        return "Uri{" +
                "uri=" + Obfuscate.secure(UriEncoderDecoder.withoutAuthority(wrapped), "/") +
                '}';
    }
}
