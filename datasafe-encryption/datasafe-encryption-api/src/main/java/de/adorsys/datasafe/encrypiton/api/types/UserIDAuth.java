package de.adorsys.datasafe.encrypiton.api.types;

import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.types.api.utils.Obfuscate;
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

    public UserIDAuth(String userID, String readKeyPassword) {
        this.userID = new UserID(userID);
        this.readKeyPassword = new ReadKeyPassword(readKeyPassword);
    }

    @Override
    public String toString() {
        return "UserIDAuth{" +
                "userID=" + Obfuscate.secure(userID) +
                ", readKeyPassword=" + Obfuscate.secure(readKeyPassword) +
                '}';
    }
}
