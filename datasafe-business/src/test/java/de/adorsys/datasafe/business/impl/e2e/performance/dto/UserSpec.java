package de.adorsys.datasafe.business.impl.e2e.performance.dto;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.business.impl.e2e.performance.services.ContentGenerator;
import lombok.Data;

@Data
public class UserSpec {

    private final UserIDAuth auth;
    private final ContentGenerator generator;
}
