package de.adorsys.datasafe.business.impl.e2e.performance;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import lombok.Data;

@Data
public class UserSpec {

    private final UserIDAuth auth;
    private final ContentGenerator generator;
}
