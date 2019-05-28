package de.adorsys.datasafe.metainfo.version.impl.version.types;

import de.adorsys.datasafe.types.api.resource.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DFSVersion implements Version {

    private final String id;
}
