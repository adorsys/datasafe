package de.adorsys.datasafe.cli.commands.inbox;

import de.adorsys.datasafe.types.api.actions.ListRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = "Lists file in INBOX"
)
public class List implements Runnable {

    @CommandLine.ParentCommand
    private Inbox inbox;

    @CommandLine.Parameters(arity = "0..1")
    private String prefix = "";

    @Override
    public void run() {
        inbox.getCli().datasafe().inboxService()
                .list(ListRequest.forDefaultPrivate(inbox.getCli().auth(), prefix))
                .forEach(it -> System.out.println(it.getResource().asPrivate().decryptedPath().asString()));
    }
}
