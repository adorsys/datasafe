package de.adorsys.datasafe.business.api.types.privatespace;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import lombok.Data;

@Data
public class PrivateWriteRequest {

    private final UserIDAuth owner;
    private final FileIn request;
}
