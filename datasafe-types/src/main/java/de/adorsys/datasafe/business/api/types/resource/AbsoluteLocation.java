package de.adorsys.datasafe.business.api.types.resource;

import de.adorsys.datasafe.business.api.types.utils.Log;
import lombok.Getter;

import java.net.URI;

public class AbsoluteLocation<T extends ResourceLocation<T>> implements ResourceLocation<T> {

    @Getter
    private final T resource;

    public AbsoluteLocation(T resource) {
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

    @Override
    public String toString() {
        return "AbsoluteLocation{" +
                "resource=" + Log.secure(location()) +
                '}';
    }
}
