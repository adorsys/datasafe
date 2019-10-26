package de.adorsys.datasafe.cli.commands.profile.storage.credentials;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "add",
        description = "Adds path mapping and credentials for it (i.e. credentials to access s3://.+)"
)
public class Add implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Credentials credentials;

    @CommandLine.Option(
            names = {"--mapping", "-m"},
            description = "Storage mapping regex (i.e. 's3://.+' will route all requests with s3 protocol to this storage)",
            required = true)
    private String mapping;

    @CommandLine.Option(
            names = {"--username", "-u"},
            description = "Storage username (i.e. AWS_ACCESS_KEY)",
            required = true)
    private String username;

    @CommandLine.Option(
            names = {"--password", "-p"},
            description = "Storage password (i.e. AWS_SECRET_KEY)",
            interactive = true,
            required = true)
    private String password;

    @Override
    public void run() {
        Cli cli = credentials.getStorage().getProfile().getCli();
        cli.datasafe().userProfile().registerStorageCredentials(
                cli.auth(),
                new StorageIdentifier(mapping),
                new StorageCredentials(username, password)
        );
    }
}
