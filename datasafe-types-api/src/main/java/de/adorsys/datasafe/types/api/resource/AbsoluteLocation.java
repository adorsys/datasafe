package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;

/**
 * Wrapper that forces underlying resource {@code T} to be absolute (same meaning as absolute URI).
 * @param <T> Wrapped resource
 */
public class AbsoluteLocation<T extends ResourceLocation<T>> implements ResourceLocation<T> {

    @Getter
    private final T resource;

    public AbsoluteLocation(T resource) {
        if (!resource.location().isAbsolute()) {
            throw new IllegalArgumentException("Resource location must be absolute " + resource);
        }

        this.resource = resource;
    }

    @Override
    public T resolveFrom(ResourceLocation absolute) {
        return resource.resolveFrom(absolute);
    }

    @Override
    public Uri location() {
        return resource.location();
    }

    @Override
    public String toString() {
        return "AbsoluteLocation{" +
                "resource=" + location() +
                '}';
    }
}
