package de.adorsys.datasafe.cli.commands.profile;

import picocli.CommandLine;

@CommandLine.Command(
        name = "cat",
        description = "Reads and prints user profile to STDOUT"
)
public class Read implements Runnable {

    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {

    }
}
