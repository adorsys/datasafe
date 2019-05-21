package de.adorsys.datasafe.business.impl.version.types;

import de.adorsys.datasafe.business.api.types.resource.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DFSVersion implements Version {

    private final String id;
}
