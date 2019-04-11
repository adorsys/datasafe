package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.file.FileIn;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class WriteRequest {

    @NonNull
    private final DFSAccess to;

    @NonNull
    private final FileIn data;

    @NonNull
    private final PublicKeyWithId keyWithId;
}