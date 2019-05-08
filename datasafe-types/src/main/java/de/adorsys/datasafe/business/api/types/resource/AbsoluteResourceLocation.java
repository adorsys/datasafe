package de.adorsys.datasafe.business.api.types.resource;

import lombok.Getter;
import lombok.ToString;

import java.net.URI;

@ToString
public class AbsoluteResourceLocation<T extends ResourceLocation<T>> implements ResourceLocation<T> {

    @Getter
    private final T resource;

    public AbsoluteResourceLocation(T resource) {
        if (!resource.location().isAbsolute()) {
            throw new IllegalArgumentException("Resource location must be absolute");
        }

        this.resource = resource;
    }

    @Override
    public T resolve(ResourceLocation absolute) {
        return resource.resolve(absolute);
    }

    @Override
    public URI location() {
        return resource.location();
    }
}
