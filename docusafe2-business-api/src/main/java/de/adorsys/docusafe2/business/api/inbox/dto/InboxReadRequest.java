package de.adorsys.docusafe2.business.api.inbox.dto;

import de.adorsys.docusafe2.business.api.types.InboxBucketPath;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import de.adorsys.docusafe2.business.api.types.file.FileOut;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InboxReadRequest {

    @NonNull
    private final UserIdAuth owner;

    @NonNull
    private final InboxBucketPath path;

    @NonNull
    private final FileOut response;
}
