package de.adorsys.datasafe.cli.commands.profile;

import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Removes user profile and his files"
)
public class Delete implements Runnable {

    @Override
    public void run() {

    }
}
