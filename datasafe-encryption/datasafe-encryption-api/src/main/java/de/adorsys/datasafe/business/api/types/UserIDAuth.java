package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "userID")
public class UserIDAuth {

    private UserID userID;
    private ReadKeyPassword readKeyPassword;
}
