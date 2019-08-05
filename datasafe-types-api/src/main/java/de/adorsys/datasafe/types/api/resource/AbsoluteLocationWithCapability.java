package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;

/**
 * Absolute resource location that can tell {@code StorageService} to behave differently.
 * @param <T> Wrapped resource
 */
@Getter
public class AbsoluteLocationWithCapability<T extends ResourceLocation<T>> extends AbsoluteLocation<T> {

    private final StorageCapability capability;

    public AbsoluteLocationWithCapability(T resource, StorageCapability capability) {
        super(resource);
        this.capability = capability;
    }
}
