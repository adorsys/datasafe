package de.adorsys.datasafe.cli.commands.inbox;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.InputStream;

@CommandLine.Command(
        name = "cat",
        description = "Decrypts inbox file contents and prints it to STDOUT"
)
public class Cat implements Runnable {

    @CommandLine.ParentCommand
    private Inbox inbox;

    @CommandLine.Parameters(description = "Filename to print", arity = "1")
    private String path;

    @Override
    @SneakyThrows
    public void run() {
        try (InputStream is = inbox.getCli().datasafe().inboxService()
                .read(ReadRequest.forDefaultPrivate(inbox.getCli().auth(), path))) {
            ByteStreams.copy(is, System.out);
        }
    }
}
