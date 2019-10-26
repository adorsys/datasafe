package de.adorsys.datasafe.cli.commands.profile;

import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Removes user profile and his files"
)
public class Delete implements Runnable {

    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {
        profile.getCli().datasafe().userProfile().deregister(profile.getCli().auth());
    }
}
