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

    public String getPath() {
        return wrapped.getPath();
    }

    public Uri resolve(String uri) {
        return new Uri(this.wrapped.resolve(uri));
    }

    public Uri resolve(URI uri) {
        return resolve(new Uri(uri));
    }

    public Uri resolve(Uri uri) {
        return new Uri(this.wrapped.resolve(uri.getWrapped()));
    }

    public Uri relativize(Uri uri) {
        return new Uri(this.wrapped.relativize(uri.getWrapped()));
    }

    public Uri asDir() {
        return new Uri(this.wrapped.toASCIIString() + "/");
    }

    public String toASCIIString() {
        return this.wrapped.toASCIIString();
    }

    public boolean isAbsolute() {
        return this.wrapped.isAbsolute();
    }

    public boolean isEmpty() {
        return this.wrapped.toASCIIString().isEmpty();
    }

    public boolean isDir() {
        return this.wrapped.toASCIIString().endsWith("/");
    }

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
