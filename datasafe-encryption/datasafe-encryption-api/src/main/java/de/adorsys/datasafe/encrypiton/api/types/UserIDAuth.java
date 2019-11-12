package de.adorsys.datasafe.encrypiton.api.types;

import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

/**
 * Wrapper that represents username and password.
 */
@Getter
@EqualsAndHashCode(of = "userID")
@RequiredArgsConstructor
public class UserIDAuth {

    private final UserID userID;
    private final ReadKeyPassword readKeyPassword;

    public UserIDAuth(String userID, ReadKeyPassword readKeyPassword) {
        this.userID = new UserID(userID);
        this.readKeyPassword = readKeyPassword;
    }

    public UserIDAuth(String userID, Supplier<char[]> readKeyPassword) {
        this.userID = new UserID(userID);
        this.readKeyPassword = new ReadKeyPassword(readKeyPassword);
    }

    @Override
    public String toString() {
        return "UserIDAuth{" +
                "userID=" + userID +
                ", readKeyPassword=" + readKeyPassword +
                '}';
    }
}
