package de.adorsys.datasafe.cli.commands.inbox;

import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "share",
        description = "Shares file with other users, " +
                "file will be encrypted using recipient public key - only recipient can read it"
)
public class Share implements Runnable {

    @CommandLine.ParentCommand
    private Inbox inbox;

    @CommandLine.Option(names = {"--share", "-s"}, description = "Which file to share", required = true)
    private Path path;

    @CommandLine.Option(
            names = {"--filename", "-f"},
            description = "How to name file in recipients' INBOX",
            required = true)
    private String filename;

    @CommandLine.Option(names = {"--recipients", "-r"}, description = "Recipients of the file", required = true)
    private List<String> recipients;

    @Override
    @SneakyThrows
    public void run() {
        try (OutputStream os = inbox.getCli().datasafe().inboxService()
                .write(
                        WriteRequest.forDefaultPublic(
                                recipients.stream().map(UserID::new).collect(Collectors.toSet()),
                                filename
                        )
                );
             InputStream is = MoreFiles.asByteSource(path, StandardOpenOption.READ).openStream()
        ) {
            ByteStreams.copy(is, os);
        }
    }
}
