package de.adorsys.datasafe.business.api.types;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.utils.Log;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(of = "userID")
@RequiredArgsConstructor
public class UserIDAuth {

    private final UserID userID;
    private final ReadKeyPassword readKeyPassword;

    @Override
    public String toString() {
        return "UserIDAuth{" +
                "userID=" + Log.secure(userID) +
                ", readKeyPassword=" + Log.secure(readKeyPassword) +
                '}';
    }
}
