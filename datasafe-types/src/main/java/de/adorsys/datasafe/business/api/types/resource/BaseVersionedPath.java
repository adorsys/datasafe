package de.adorsys.datasafe.business.api.types.resource;

import lombok.Getter;

public class BaseVersionedPath<A extends ResourceLocation<A>, R, V extends Version>
        implements Versioned<AbsoluteLocation<A>, R, V> {

    @Getter
    private final V version;

    private final AbsoluteLocation<A> withVersion;
    private final R withoutVersion;


    public BaseVersionedPath(
            V version,
            AbsoluteLocation<A> withVersion,
            R withoutVersion) {
        this.version = version;
        this.withVersion = withVersion;
        this.withoutVersion = withoutVersion;
    }

    @Override
    public AbsoluteLocation<A> absolute() {
        return withVersion;
    }

    @Override
    public R stripVersion() {
        return withoutVersion;
    }
}
