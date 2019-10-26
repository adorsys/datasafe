package de.adorsys.datasafe.cli.commands.profile.storage;

import de.adorsys.datasafe.cli.commands.profile.Profile;
import de.adorsys.datasafe.cli.commands.profile.storage.credentials.Credentials;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "storage",
        description = "Manages user storage list (i.e. adds another s3 storage path)",
        subcommands = {
                Add.class,
                List.class,
                Remove.class,
                Credentials.class
        }
)
public class Storage implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {
        CommandLine.usage(new Storage(), System.out);
    }
}
