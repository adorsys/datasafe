package de.adorsys.datasafe.types.api.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class indicates file version used by storage system (i.e. when you have S3 versioned bucket
 * it automatically creates versions for you and this class handles this case)
 */
@Getter
@RequiredArgsConstructor
public class StorageVersion implements Version {

    private final String versionId;
}
