package de.adorsys.datasafe.cli.commands.profile.storage;

import de.adorsys.datasafe.cli.Cli;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = "Lists path mapping aliases to access user storage"
)
public class List implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Storage storage;

    @Override
    public void run() {
        Cli cli = storage.getProfile().getCli();

        cli.datasafe().userProfile().privateProfile(
                cli.auth()
        ).getPrivateStorage().keySet().forEach(it -> System.out.println(it.getId()));
    }
}
