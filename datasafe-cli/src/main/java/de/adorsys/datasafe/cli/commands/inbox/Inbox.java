package de.adorsys.datasafe.cli.commands.inbox;

import de.adorsys.datasafe.cli.Cli;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "inbox",
        description =
                "Allows user to read encrypted files that are shared with him and to share his files with other users",
        subcommands = {
                Cat.class,
                List.class,
                Share.class,
                Delete.class,
})
public class Inbox implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Cli cli;

    @Override
    public void run() {
        CommandLine.usage(new Inbox(), System.out);
    }
}
