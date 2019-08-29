package de.adorsys.datasafe.cli.commands.inbox;

import picocli.CommandLine;

@CommandLine.Command(
        name = "inbox",
        description =
                "Allows user to read encrypted files that are shared with him and to share his files with other users",
        subcommands = {
                Cat.class,
                Copy.class,
                Delete.class,
        })
public class Inbox implements Runnable {

    @Override
    public void run() {

    }
}
