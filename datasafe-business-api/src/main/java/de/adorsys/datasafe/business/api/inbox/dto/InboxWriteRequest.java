package de.adorsys.datasafe.business.api.inbox.dto;

import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import lombok.Data;

@Data
public class InboxWriteRequest {

    private final UserID from;
    private final UserID to;
    private final FileIn request;
}
