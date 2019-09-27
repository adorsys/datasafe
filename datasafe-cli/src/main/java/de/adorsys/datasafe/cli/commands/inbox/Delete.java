package de.adorsys.datasafe.cli.commands.inbox;

import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.resource.Uri;
import picocli.CommandLine;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Deletes file from your inbox"
)
public class Delete implements Runnable {

    @CommandLine.ParentCommand
    private Inbox inbox;

    @CommandLine.Parameters(description = "Filename to remove", arity = "1")
    private String path;

    @Override
    public void run() {
        inbox.getCli().datasafe().inboxService()
                .remove(RemoveRequest.forDefaultPrivate(inbox.getCli().auth(), new Uri(path)));
    }
}
