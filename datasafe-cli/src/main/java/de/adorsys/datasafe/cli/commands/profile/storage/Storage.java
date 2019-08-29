package de.adorsys.datasafe.cli.commands.profile.storage;

import de.adorsys.datasafe.cli.commands.profile.Profile;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "storage",
        description = "Updates user storage list (i.e. adds another s3 storage definition)",
        subcommands = {
                Add.class,
                List.class,
                Remove.class
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
