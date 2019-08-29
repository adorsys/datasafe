package de.adorsys.datasafe.cli.commands.inbox;

import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "copy",
        aliases = "cp",
        description = "Shares file with other users, " +
                "file will be encrypted using recipient public key - only recipient can read it"
)
public class Share implements Runnable {

    @CommandLine.ParentCommand
    private Inbox inbox;

    @CommandLine.Option(names = {"--share", "-s"}, description = "Which file to share, will read from STDIN if absent")
    private Path path;

    @CommandLine.Option(names = {"--filename", "-f"}, description = "How to name file in recipients' INBOX")
    private String filename;

    @CommandLine.Option(names = {"--recipients", "-r"}, description = "Recipients of the file")
    private List<String> recipients;

    @Override
    @SneakyThrows
    public void run() {
        String destinationName = getFilename();
        try (OutputStream os = inbox.getCli().datasafe().inboxService()
                .write(
                        WriteRequest.forDefaultPublic(
                                recipients.stream().map(UserID::new).collect(Collectors.toSet()),
                                destinationName
                        )
                );
             InputStream is = getInputStream()
        ) {
            ByteStreams.copy(is, os);
        }
    }

    private String getFilename() {
        return null == filename ? getFilenameFromPath() : filename;
    }

    private String getFilenameFromPath() {
        if (null == path) {
            throw new IllegalArgumentException("Filename is required");
        }

        return path.getFileName().toString();
    }

    private InputStream getInputStream() throws IOException {
        return null != path ? MoreFiles.asByteSource(path, StandardOpenOption.READ).openStream() : System.in;
    }
}
