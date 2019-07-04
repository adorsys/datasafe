package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.dto;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.shared.ContentGenerator;
import lombok.Data;

/**
 * User credentials and his content generator (used to write files).
 */
@Data
public class UserSpec {

    private final UserIDAuth auth;
    private final ContentGenerator generator;
}
