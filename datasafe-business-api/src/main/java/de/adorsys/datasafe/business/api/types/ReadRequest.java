package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.deployment.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ReadRequest {

    @NonNull
    private final DFSAccess from;

    @NonNull
    private final FileOut response;

    @NonNull
    private final KeyStoreAccess keyStore;
}
