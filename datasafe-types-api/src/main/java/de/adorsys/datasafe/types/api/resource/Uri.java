package de.adorsys.datasafe.types.api.resource;

import com.google.common.net.UrlEscapers;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
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
            this.wrapped = new URI(UrlEscapers.urlFragmentEscaper().escape(path));
        } catch (URISyntaxException ex) {
            throw new URISyntaxException(Obfuscate.secure(ex.getInput(), "/"), ex.getReason());
        }
    }

    /**
     * {@link URI#getPath()}
     * @return Path part of wrapped URI.
     */
    public String getPath() {
        return wrapped.getPath();
    }

    /**
     * {@link URI#resolve(String)}
     * @param uri Path to resolve.
     * @return Uri that has <b>wrapped Uri + {@code uri}</b> as its path.
     */
    public Uri resolve(String uri) {
        return new Uri(this.wrapped.resolve(UrlEscapers.urlPathSegmentEscaper().escape(uri)));
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
        return isDir() ? this : new Uri(this.wrapped.toASCIIString() + "/");
    }

    /**
     * @return Makes directory from underlying resource
     * For example, http://example.com/foo becomes http://example.com/foo/ so it can be resolved against.
     */
    public Uri asFile() {
        return isDir() ? new Uri(this.wrapped.toASCIIString().replaceAll("/$", "")) : this;
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
        return this.wrapped.toASCIIString().isEmpty();
    }

    /**
     * @return If this resource can act as path root.
     * @implNote  For example "http://example.com/foo".resolve("bar") transforms to http://example.com/bar,
     * so it can't be path root.
     * "http://example.com/foo/".resolve("bar") transforms to http://example.com/foo/bar, so it is path root.
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

    /**
     * @return wrapped resource without authority
     */
    public URI withoutAuthority() {
        return URI.create(withoutAuthority(wrapped));
    }

    /**
     * Returns URL-decoded representation of this class, and strips authority
     * @return URL-decoded value (i.e. %20 will become ' ')
     */
    public String asString() {
        return withoutAuthority(wrapped);
    }

    @Override
    public String toString() {
        return "Uri{" +
                "uri=" + Obfuscate.secure(withoutAuthority(wrapped), "/") +
                '}';
    }

    private String withoutAuthority(URI uri) {
        if (uri == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (null != uri.getScheme()) {
            sb.append(uri.getScheme()).append("://");
        }

        if (null != uri.getHost()) {
            sb.append(uri.getHost());
        }

        if (-1 != uri.getPort()) {
            sb.append(":");
            sb.append(uri.getPort());
        }

        if (null != uri.getPath()) {
            sb.append(uri.getPath());
        }

        return sb.toString();
    }
}
