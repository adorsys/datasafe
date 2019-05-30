package de.adorsys.datasafe.business.impl.e2e.performance.dto;

import de.adorsys.datasafe.business.impl.e2e.performance.services.ContentGenerator;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.Data;

/**
 * User credentials and his content generator (used to write files).
 */
@Data
public class UserSpec {

    private final UserIDAuth auth;
    private final ContentGenerator generator;
}
