package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.keystore.types.ReadKeyPassword;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIDAuth {
    private UserID userID;
    private ReadKeyPassword readKeyPassword;
}
