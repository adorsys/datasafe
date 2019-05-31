package de.adorsys.datasafe.encrypiton.api.types;

import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.Log;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper that represents username and password.
 */
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
