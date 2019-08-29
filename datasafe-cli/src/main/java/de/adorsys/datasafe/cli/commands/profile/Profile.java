package de.adorsys.datasafe.cli.commands.profile;

import de.adorsys.datasafe.cli.commands.inbox.Delete;
import picocli.CommandLine;

@CommandLine.Command(
        name = "profile",
        description = "Manages user profile - reads/creates/updates it",
        subcommands = {
                Create.class,
                Update.class,
                Read.class,
                Delete.class,
        })
public class Profile implements Runnable {

    @Override
    public void run() {

    }
}
