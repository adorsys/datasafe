package de.adorsys.datasafe.business.api.types.resource;

import lombok.Getter;

public class BaseVersionedPath<V extends Version>
        implements Versioned<AbsoluteResourceLocation<PrivateResource>, PrivateResource, V> {

    @Getter
    private final V version;

    private final PrivateResource withoutVersion;
    private final AbsoluteResourceLocation<PrivateResource> withVersion;

    public BaseVersionedPath(
            V version,
            PrivateResource withoutVersion,
            AbsoluteResourceLocation<PrivateResource> withVersion) {
        this.version = version;
        this.withoutVersion = withoutVersion;
        this.withVersion = withVersion;
    }

    @Override
    public AbsoluteResourceLocation<PrivateResource> absolute() {
        return withVersion;
    }

    @Override
    public PrivateResource stripVersion() {
        return withoutVersion;
    }
}
