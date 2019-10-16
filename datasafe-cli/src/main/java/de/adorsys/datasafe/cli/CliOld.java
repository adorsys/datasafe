package de.adorsys.datasafe.cli;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import lombok.SneakyThrows;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.time.LocalDateTime;

public class CliOld {

    @SneakyThrows
    public static void main(String[] args) {
        Path root = Paths.get("/home/valb3r/temp/datasafe-tst/" + LocalDateTime.now().toString() + "/");
        Files.createDirectories(root);

        Security.addProvider(new BouncyCastleProvider());
        // To register provider you need to:
        /*
            Share the JCE provider JAR file to java-home/jre/lib/ext/.
            Stop the Application Server.
            If the Application Server is not stopped and then restarted later in this process, the JCE provider will not be recognized by the Application Server.
            Edit the java-home/jre/lib/security/java.security properties file in any text editor. Add the JCE provider you’ve just downloaded to this file.
            The java.security file contains detailed instructions for adding this provider. Basically, you need to add a line of the following format in a location with similar properties:
            security.provider.n=provider-class-name
         */

        // this will create all Datasafe files and user documents under <temp dir path>
        DefaultDatasafeServices datasafe = datasafeServices(root, "PAZZWORT");

        UserIDAuth user = new UserIDAuth("me", "mememe");
        UserIDAuth userRecipient = new UserIDAuth("recipient", "RRRE!!");

        datasafe.userProfile().registerUsingDefaults(user);
        datasafe.userProfile().registerUsingDefaults(userRecipient);

        try (OutputStream os =
                     datasafe.privateService().write(WriteRequest.forDefaultPrivate(user, "my-file"))
        ) {
            os.write("Hello from Datasafe".getBytes());
        }


        long sz = datasafe.privateService().list(ListRequest.forDefaultPrivate(user, "./")).count();
        System.out.println("User has " + sz + " files");

        System.out.println(new String(
                ByteStreams.toByteArray(
                        datasafe.privateService().read(ReadRequest.forDefaultPrivate(user, "my-file"))
                )
        ));

        try (OutputStream os =
                     datasafe.inboxService().write(WriteRequest.forDefaultPublic(
                             ImmutableSet.of(user.getUserID(), userRecipient.getUserID()), "hello-recipient"))
        ) {
            os.write("Hello from INBOX!".getBytes());
        }

        long szInb = datasafe.inboxService().list(ListRequest.forDefaultPrivate(user, "./")).count();
        System.out.println("User has " + szInb + " files in INBOX");

        System.out.println(new String(
                ByteStreams.toByteArray(
                        datasafe.inboxService().read(ReadRequest.forDefaultPrivate(user, "hello-recipient"))
                )
        ));
    }

    private static DefaultDatasafeServices datasafeServices(Path fsRoot, String systemPassword) {
        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DefaultDFSConfig(fsRoot.toUri().toASCIIString(), systemPassword))
                .storage(
                        new FileSystemStorageService(fsRoot)
                )
                .build();
        return multiDfsDatasafe;
    }
}

