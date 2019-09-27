package de.adorsys.datasafe.cli.commands.profile;

import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import picocli.CommandLine;

import java.util.Collections;

import static de.adorsys.datasafe.cli.commands.profile.InputUtil.inpPath;

@CommandLine.Command(
        name = "create",
        description = "Creates new user profile (new user)"
)
public class Create implements Runnable {

    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {
        String user = profile.getCli().auth().getUserID().getValue();
        Uri publicKeys = inpPath("Public keys", atRoot(user, "pubkeys"));
        Uri inbox = inpPath("Your INBOX folder", atRoot(user, "inbox/"));

        CreateUserPublicProfile publicProfile = CreateUserPublicProfile.builder()
                .id(profile.getCli().auth().getUserID())
                .publicKeys(BasePublicResource.forAbsolutePublic(publicKeys))
                .inbox(BasePublicResource.forAbsolutePublic(inbox))
                .build();

        profile.getCli().datasafe().userProfile().registerPublic(publicProfile);

        CreateUserPrivateProfile privateProfile = CreateUserPrivateProfile.builder()
                .id(profile.getCli().auth())
                .keystore(BasePrivateResource.forAbsolutePrivate(inpPath("Keystore", atRoot(user, "keystore"))))
                .privateStorage(BasePrivateResource.forAbsolutePrivate(inpPath("Private files", atRoot(user, "private/"))))
                .inboxWithWriteAccess(BasePrivateResource.forAbsolutePrivate(inbox))
                .storageCredentialsKeystore(
                        BasePrivateResource.forAbsolutePrivate(
                                inpPath("Storage credentials keystore", atRoot(user, "storage.keystore"))
                        )
                )
                .associatedResources(Collections.emptyList())
                .publishPubKeysTo(BasePublicResource.forAbsolutePublic(publicKeys))
                .build();

        profile.getCli().datasafe().userProfile().registerPrivate(privateProfile);
        profile.getCli().datasafe().userProfile().createAllAllowableKeystores(
                profile.getCli().auth(),
                privateProfile.buildPrivateProfile()
        );
    }

    private String atRoot(String username, String name) {
        String resolved = profile
                .getCli()
                .getProfilesRoot()
                .resolve(username)
                .resolve(name)
                .toAbsolutePath()
                .toUri()
                .toASCIIString();

        return (name.endsWith("/") && !resolved.endsWith("/") ? resolved + "/" : resolved);
    }
}
