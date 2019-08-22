package de.adorsys.datasafe.types.api.resource;

import lombok.RequiredArgsConstructor;

import java.net.URI;

/**
 * Base class for public (shareable) resource.
 */
@RequiredArgsConstructor
public class BasePublicResource implements PublicResource {

    private final Uri uri;

    public BasePublicResource(URI uri) {
        this.uri = new Uri(uri);
    }

    public static AbsoluteLocation<PublicResource> forAbsolutePublic(Uri path) {
        return new AbsoluteLocation<>(new BasePublicResource(path));
    }

    public static AbsoluteLocation<PublicResource> forAbsolutePublic(URI path) {
        return forAbsolutePublic(new Uri(path));
    }

    public static AbsoluteLocation<PublicResource> forAbsolutePublic(String path) {
        return forAbsolutePublic(new Uri(path));
    }

    @Override
    public Uri location() {
        return uri;
    }

    @Override
    public PublicResource resolveFrom(ResourceLocation location) {
        return new BasePublicResource(location.location().resolve(uri));
    }


    @Override
    public String toString() {
        return "BasePublicResource{" +
                "uri=" + uri +
                '}';
    }
}
