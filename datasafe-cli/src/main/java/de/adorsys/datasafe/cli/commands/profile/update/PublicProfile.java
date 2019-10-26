package de.adorsys.datasafe.cli.commands.profile.update;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import lombok.Getter;
import picocli.CommandLine;

import static de.adorsys.datasafe.cli.commands.profile.InputUtil.inpPath;

@CommandLine.Command(
        name = "public",
        description = "Updates user public part of user profile"
)
public class PublicProfile implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Update update;

    @Override
    public void run() {
        Cli cli = update.getProfile().getCli();
        UserPublicProfile current = cli.datasafe().userProfile().publicProfile(cli.auth().getUserID());

        UserPublicProfile publicProfile = UserPublicProfile.builder()
                .inbox(BasePublicResource.forAbsolutePublic(inpPath("Inbox", asStr(current.getPublicKeys()))))
                .publicKeys(BasePublicResource.forAbsolutePublic(inpPath("Public keys", asStr(current.getInbox()))))
                .build();

        cli.datasafe().userProfile().updatePublicProfile(cli.auth(), publicProfile);
    }

    private String asStr(AbsoluteLocation location) {
        return location.location().asString();
    }

}

