package de.adorsys.datasafe.cli.commands.profile.storage.credentials;

import de.adorsys.datasafe.cli.commands.profile.storage.Storage;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "credentials",
        description = "Manages user storage credentials (i.e. your credentials to access S3 bucket)",
        subcommands = {
                Add.class,
                List.class,
                Remove.class
        }
)
public class Credentials implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Storage storage;

    @Override
    public void run() {
        CommandLine.usage(new Credentials(), System.out);
    }
}
