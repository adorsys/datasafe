package de.adorsys.docusafe2.business.api.inbox.dto;

import de.adorsys.docusafe2.business.api.types.file.FileOut;
import de.adorsys.docusafe2.business.api.types.InboxBucketPath;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import lombok.Data;

@Data
public class InboxReadRequest {

    private final UserIdAuth owner;
    private final InboxBucketPath path;
    private final FileOut response;
}
