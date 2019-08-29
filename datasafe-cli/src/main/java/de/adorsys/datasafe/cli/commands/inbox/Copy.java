package de.adorsys.datasafe.cli.commands.inbox;

import picocli.CommandLine;

@CommandLine.Command(
        name = "copy",
        aliases = "cp",
        description = "Shares file with other users, " +
                "file will be encrypted using recipient public key - only recipient can read it"
)
public class Copy implements Runnable {

    @Override
    public void run() {

    }
}
