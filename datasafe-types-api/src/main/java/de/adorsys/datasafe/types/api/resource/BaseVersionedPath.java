package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;

/**
 * Base class for versioned resource path.
 * @param <A> Absolute (physical) resource location
 * @param <R> Logical resource location (resource without version)
 * @param <V> Version tag class
 */
public class BaseVersionedPath<A extends ResourceLocation<A>, R, V extends Version>
        implements Versioned<AbsoluteLocation<A>, R, V> {

    @Getter
    private final V version;

    private final AbsoluteLocation<A> withVersion;
    private final R withoutVersion;


    /**
     * @param version Version tag class
     * @param withVersion Physical resource location
     * @param withoutVersion Logical resource location without version
     */
    public BaseVersionedPath(
            V version,
            AbsoluteLocation<A> withVersion,
            R withoutVersion) {
        this.version = version;
        this.withVersion = withVersion;
        this.withoutVersion = withoutVersion;
    }

    /**
     * @return Absolute path to the resource
     */
    @Override
    public AbsoluteLocation<A> absolute() {
        return withVersion;
    }

    /**
     * @return Logical path to the resource that does not contain its version
     */
    @Override
    public R stripVersion() {
        return withoutVersion;
    }
}
