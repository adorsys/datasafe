package de.adorsys.datasafe.rest.impl.dto;

import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.types.api.global.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicProfileDTO {

    @NotBlank
    private String publicKeys;

    @NotBlank
    private String inbox;

    public static UserPublicProfileDTO from(UserPublicProfile publicProfile) {
        return new UserPublicProfileDTO(
            Util.str(publicProfile.getPublicKeys()),
            Util.str(publicProfile.getInbox())
        );
    }

    public UserPublicProfile toProfile() {
        return UserPublicProfile.builder()
            .inbox(Util.publicResource(inbox))
            .publicKeys(Util.publicResource(publicKeys))
            .appVersion(Version.current())
            .build();
    }
}
