package de.adorsys.datasafe.cli.commands.privatespace;

import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.SneakyThrows;
import picocli.CommandLine;

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

    @CommandLine.Parameters(description = "Path to copy from", index = "0", arity = "1")
    private Path from;

    @CommandLine.Parameters(description = "Path in privatespace to copy to", index = "1", arity = "1")
    private String to;

    @Override
    @SneakyThrows
    public void run() {
        try (OutputStream os = privatespace.getCli().datasafe().privateService()
                .write(
                        WriteRequest.forPrivate(
                                privatespace.getCli().auth(),
                                new StorageIdentifier(storageId),
                                to
                        )
                );
             InputStream is = MoreFiles.asByteSource(from, StandardOpenOption.READ).openStream()
        ) {
            ByteStreams.copy(is, os);
        }
    }
}
