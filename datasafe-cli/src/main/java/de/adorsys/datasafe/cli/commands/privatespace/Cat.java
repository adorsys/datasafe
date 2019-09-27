package de.adorsys.datasafe.cli.commands.privatespace;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.SneakyThrows;
import picocli.CommandLine;

import java.io.InputStream;

@CommandLine.Command(
        name = "cat",
        description = "Decrypts private file contents and prints it to STDOUT"
)
public class Cat implements Runnable {

    @CommandLine.ParentCommand
    private Privatespace privatespace;

    @CommandLine.Option(names = {"--storage", "-s"}, description = "Storage identifier")
    private String storageId = StorageIdentifier.DEFAULT_ID;

    @CommandLine.Parameters(description = "Filename to decrypt and print", arity = "1")
    private String path;

    @Override
    @SneakyThrows
    public void run() {
        try (InputStream is = privatespace.getCli().datasafe().privateService()
                .read(ReadRequest.forPrivate(privatespace.getCli().auth(), new StorageIdentifier(storageId), path))) {
            ByteStreams.copy(is, System.out);
        }
    }
}
