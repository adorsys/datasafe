package de.adorsys.datasafe.cli.commands.inbox;

import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Deletes file from your inbox"
)
public class Delete implements Runnable {

    @Override
    public void run() {

    }
}
