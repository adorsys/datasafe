package de.adorsys.datasafe.cli.commands.privatespace;

import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Removes file from your privatespace"
)
public class Delete implements Runnable {

    @CommandLine.ParentCommand
    private Privatespace privatespace;

    @CommandLine.Option(names = {"--storage", "-s"}, description = "Storage identifier")
    private String storageId = StorageIdentifier.DEFAULT_ID;

    @CommandLine.Parameters(description = "Filename to remove", arity = "1")
    private String path;

    @Override
    public void run() {
        privatespace.getCli().datasafe().privateService()
                .remove(RemoveRequest.forPrivate(privatespace.getCli().auth(), new StorageIdentifier(storageId), path));
    }
}
