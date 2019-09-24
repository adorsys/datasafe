package de.adorsys.datasafe.rest.impl.dto;

import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.types.api.global.Version;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivateProfileDTO {

    @NotBlank
    private String keystore;

    /**
     * Users' privatespace location (where his private files are stored)
     */
    @NotEmpty
    private Map<String, String> privateStorage;

    /**
     * Users' INBOX as privatespace location (INBOX folder with full control)
     */
    @NotBlank
    private String inboxWithFullAccess;

    /**
     * Where one should publish public keys.
     */
    @NotBlank
    private String publishPublicKeysTo;

    /**
     * If all files reside within some specific folder, one can simply remove it when deregistering user,
     * instead of removing files one-by-one - this is the list of such folders, or if we need to remove extra
     * associated resources with user.
     */
    @NotNull
    private List<String> associatedResources;


    private String documentVersionStorage;

    private String storageCredentialsKeystore;

    public static UserPrivateProfileDTO from(UserPrivateProfile privateProfile) {

        return new UserPrivateProfileDTO(
            Util.str(privateProfile.getKeystore()),
            privateProfile.getPrivateStorage().entrySet().stream().collect(
                Collectors.toMap(it -> it.getKey().getId(), it -> Util.str(it.getValue()))
            ),
            Util.str(privateProfile.getInboxWithFullAccess()),
            Util.str(privateProfile.getPublishPublicKeysTo()),
            privateProfile.getAssociatedResources().stream().map(Util::str).collect(Collectors.toList()),
            Util.str(privateProfile.getDocumentVersionStorage()),
            Util.str(privateProfile.getStorageCredentialsKeystore())
        );
    }

    public UserPrivateProfile toProfile() {
        return UserPrivateProfile.builder()
            .publishPublicKeysTo(Util.publicResource(publishPublicKeysTo))
            .storageCredentialsKeystore(Util.privateResource(storageCredentialsKeystore))
            .associatedResources(associatedResources.stream().map(Util::privateResource).collect(Collectors.toList()))
            .documentVersionStorage(Util.privateResource(documentVersionStorage))
            .inboxWithFullAccess(Util.privateResource(inboxWithFullAccess))
            .keystore(Util.privateResource(keystore))
            .privateStorage(privateStorage.entrySet().stream().collect(
                    Collectors.toMap(
                            it -> new StorageIdentifier(it.getKey()),
                            it -> Util.privateResource(it.getValue())
                    ))
            )
            .appVersion(Version.current())
            .build();
    }
}
