package de.adorsys.datasafe.cli.commands.profile;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.cli.commands.profile.storage.Storage;
import de.adorsys.datasafe.cli.commands.profile.update.Update;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "profile",
        description = "Manages user profile - reads/creates/updates it",
        subcommands = {
                Create.class,
                Update.class,
                Read.class,
                Delete.class,
                Storage.class
        })
public class Profile implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Cli cli;

    @Override
    public void run() {
        CommandLine.usage(new Profile(), System.out);
    }
}
