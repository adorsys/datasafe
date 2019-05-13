package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(of = "userID")
@RequiredArgsConstructor
public class UserIDAuth {

    private final UserID userID;
    private final ReadKeyPassword readKeyPassword;
}
