package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
public class VersionedResource<V extends Version, T extends AbsoluteResourceLocation> {

    private final V version;
    private final T resource;
}
