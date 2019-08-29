package de.adorsys.datasafe.cli.commands.profile.update;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import lombok.Getter;
import picocli.CommandLine;

import java.util.Collections;

import static de.adorsys.datasafe.cli.commands.profile.InputUtil.inpPath;

@CommandLine.Command(
        name = "private",
        description = "Updates user private part of user profile"
)
public class PrivateProfile implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Update update;

    @Override
    public void run() {
        Cli cli = update.getProfile().getCli();
        UserPrivateProfile current = cli.datasafe().userProfile().privateProfile(cli.auth());

        UserPrivateProfile privateProfile = UserPrivateProfile.builder()
                .keystore(BasePrivateResource.forAbsolutePrivate(inpPath("Keystore", asStr(current.getKeystore()))))
                .privateStorage(current.getPrivateStorage())
                .inboxWithFullAccess(BasePrivateResource.forAbsolutePrivate(inpPath("Inbox", asStr(current.getInboxWithFullAccess()))))
                .storageCredentialsKeystore(
                        BasePrivateResource.forAbsolutePrivate(
                                inpPath("Storage credentials keystore", asStr(current.getStorageCredentialsKeystore()))
                        )
                )
                .associatedResources(Collections.emptyList())
                .publishPublicKeysTo(current.getPublishPublicKeysTo())
                .build();

        cli.datasafe().userProfile().updatePrivateProfile(cli.auth(), privateProfile);
    }

    private String asStr(AbsoluteLocation location) {
        return location.location().asString();
    }
}
