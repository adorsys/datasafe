package de.adorsys.datasafe.storage.impl.s3;

import de.adorsys.datasafe.types.api.resource.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class S3Version implements Version {

    private final String versionId;
}
