package de.adorsys.datasafe.cli.commands.profile.update;

import de.adorsys.datasafe.cli.commands.profile.Profile;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "update",
        description = "Updates user profile - i.e. adds another private storage",
        subcommands = {
                PrivateProfile.class,
                PublicProfile.class
        }
)
public class Update implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {
        CommandLine.usage(new Update(), System.out);
    }
}
