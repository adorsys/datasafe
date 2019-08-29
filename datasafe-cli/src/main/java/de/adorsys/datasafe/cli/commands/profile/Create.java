package de.adorsys.datasafe.cli.commands.profile;

import picocli.CommandLine;

@CommandLine.Command(
        name = "create",
        description = "Creates new user profile (new user)"
)
public class Create implements Runnable {

    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {

    }
}
