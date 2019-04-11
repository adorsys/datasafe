package de.adorsys.datasafe.business.api.inbox.dto;

import de.adorsys.datasafe.business.api.types.UserId;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import lombok.Data;

@Data
public class InboxWriteRequest {

    private final UserId from;
    private final UserId to;
    private final FileIn request;
}
