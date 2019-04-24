package de.adorsys.datasafe.business.api.types.inbox;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder
public class InboxReadRequest {

    @NonNull
    private final UserIDAuth owner;

    @NonNull
    private final URI path;

    @NonNull
    private final FileOut response;
}
