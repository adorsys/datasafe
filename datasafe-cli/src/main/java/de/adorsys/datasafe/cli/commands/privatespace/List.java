package de.adorsys.datasafe.cli.commands.privatespace;

import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = "Lists file in your privatespace"
)
public class List implements Runnable {

    @CommandLine.ParentCommand
    private Privatespace privatespace;

    @CommandLine.Option(names = {"--storage", "-s"}, description = "Storage identifier")
    private String storageId = StorageIdentifier.DEFAULT_ID;

    @CommandLine.Parameters(arity = "0..1")
    private String prefix = "";

    @Override
    public void run() {
        privatespace.getCli().datasafe().privateService()
                .list(ListRequest.forPrivate(privatespace.getCli().auth(), new StorageIdentifier(storageId), prefix))
                .forEach(it -> System.out.println(it.getResource().asPrivate().decryptedPath().asString()));
    }
}
