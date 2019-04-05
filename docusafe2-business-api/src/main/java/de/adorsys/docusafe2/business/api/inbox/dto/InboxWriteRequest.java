package de.adorsys.docusafe2.business.api.inbox.dto;

import de.adorsys.docusafe2.business.api.types.file.FileIn;
import de.adorsys.docusafe2.business.api.types.UserId;
import de.adorsys.docusafe2.business.api.types.UserIdAuth;
import lombok.Data;

@Data
public class InboxWriteRequest {

    private final UserIdAuth from;
    private final UserId to;
    private final FileIn request;
}
