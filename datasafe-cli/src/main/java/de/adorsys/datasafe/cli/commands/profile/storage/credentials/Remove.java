package de.adorsys.datasafe.cli.commands.profile.storage.credentials;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Removes path access credentials"
)
public class Remove implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Credentials credentials;

    @CommandLine.Option(
            names = {"--mapping", "-m"},
            description = "Storage mapping regex/id (i.e. 's3://.+' will route all requests with s3 protocol to this storage)",
            required = true)
    private String mapping;

    @Override
    public void run() {
        Cli cli = credentials.getStorage().getProfile().getCli();
        cli.datasafe().userProfile().deregisterStorageCredentials(
                cli.auth(),
                new StorageIdentifier(mapping)
        );
    }
}
