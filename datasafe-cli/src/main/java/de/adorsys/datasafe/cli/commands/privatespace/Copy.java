package de.adorsys.datasafe.cli.commands.privatespace;

import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@CommandLine.Command(
        name = "copy",
        aliases = "cp",
        description = "Encrypts and copies file into your private space - only you can read it"
)
public class Copy implements Runnable {

    @CommandLine.ParentCommand
    private Privatespace privatespace;

    @CommandLine.Option(names = {"--storage", "-s"}, description = "Storage identifier")
    private String storageId = StorageIdentifier.DEFAULT_ID;

    @CommandLine.Parameters(description = "Path to copy from, will read from STDIN if absent", arity = "0..1")
    private Path from;

    @CommandLine.Parameters(description = "Path in privatespace to copy to")
    private String to;

    @Override
    @SneakyThrows
    public void run() {
        String path = getDestinationPath();

        try (OutputStream os = privatespace.getCli().datasafe().privateService()
                .write(
                        WriteRequest.forPrivate(
                                privatespace.getCli().auth(),
                                new StorageIdentifier(storageId),
                                path
                        )
                );
             InputStream is = getInputStream()
        ) {
            ByteStreams.copy(is, os);
        }
    }

    private String getDestinationPath() {
        return null == to ? getDestinationPathFromFilename() : to;
    }

    private String getDestinationPathFromFilename() {
        if (null == from) {
            throw new IllegalArgumentException("Path is required");
        }

        return from.getFileName().toString();
    }

    private InputStream getInputStream() throws IOException {
        return null != from ? MoreFiles.asByteSource(from, StandardOpenOption.READ).openStream() : System.in;
    }
}
