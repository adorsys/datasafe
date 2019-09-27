package de.adorsys.datasafe.cli.commands.profile.storage.credentials;

import de.adorsys.datasafe.cli.Cli;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = "Adds path mapping and credentials for it (i.e. credentials to access s3://.+)"
)
public class List implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Credentials credentials;

    @Override
    public void run() {
        Cli cli = credentials.getStorage().getProfile().getCli();
        cli.datasafe().userProfile().listRegisteredStorageCredentials(cli.auth())
                // Omit values that exists by default in keystore FIXME - remove these from storage keystore:
                .stream()
                .filter(it -> !it.getId().startsWith("PRIVATE_SECRET") && !it.getId().startsWith("PATH_SECRET"))
                .forEach(it -> System.out.println(it.getId()));
    }
}
