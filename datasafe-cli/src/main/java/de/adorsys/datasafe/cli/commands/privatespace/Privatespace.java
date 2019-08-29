package de.adorsys.datasafe.cli.commands.privatespace;

import de.adorsys.datasafe.cli.Cli;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "private",
        description = "Allows user to read and write encrypted files in his privatespace",
        subcommands = {
                Cat.class,
                Copy.class,
                List.class,
                Delete.class,
})
public class Privatespace implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Cli cli;

    @Override
    public void run() {
        CommandLine.usage(new Privatespace(), System.out);
    }
}
