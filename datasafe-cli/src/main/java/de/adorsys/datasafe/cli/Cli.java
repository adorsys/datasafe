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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

public class Cli {

    @SneakyThrows
    public static void main(String[] args) {
        Path root = Paths.get("/Users/valentyn.berezin/IdeaProjects/datasafe/datasafe-cli/target/datasafe/");

        Security.addProvider(new BouncyCastleProvider());
        // To register provider you need to:
        /*
            Copy the JCE provider JAR file to java-home/jre/lib/ext/.
            Stop the Application Server.
            If the Application Server is not stopped and then restarted later in this process, the JCE provider will not be recognized by the Application Server.
            Edit the java-home/jre/lib/security/java.security properties file in any text editor. Add the JCE provider you’ve just downloaded to this file.
            The java.security file contains detailed instructions for adding this provider. Basically, you need to add a line of the following format in a location with similar properties:
            security.provider.n=provider-class-name
         */

        // this will create all Datasafe files and user documents under <temp dir path>
        DefaultDatasafeServices defaultDatasafeServices = DaggerDefaultDatasafeServices.builder()
                .config(new DefaultDFSConfig(root.toAbsolutePath().toUri(), "secret"))
                .storage(new FileSystemStorageService(root))
                .build();

        UserIDAuth user = new UserIDAuth("me", "mememe");
        UserIDAuth userRecipient = new UserIDAuth("recipient", "RRRE!!");

        defaultDatasafeServices.userProfile().registerUsingDefaults(user);
        defaultDatasafeServices.userProfile().registerUsingDefaults(userRecipient);

        try (OutputStream os =
                     defaultDatasafeServices.privateService().write(WriteRequest.forDefaultPrivate(user, "my-file"))
        ) {
            os.write("Hello from Datasafe".getBytes());
        }


        long sz = defaultDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(user, "./")).count();
        System.out.println("User has " + sz + " files");

        System.out.println(new String(
                ByteStreams.toByteArray(
                        defaultDatasafeServices.privateService().read(ReadRequest.forDefaultPrivate(user, "my-file"))
                )
        ));

        try (OutputStream os =
                     defaultDatasafeServices.inboxService().write(WriteRequest.forDefaultPublic(
                             ImmutableSet.of(user.getUserID(), userRecipient.getUserID()), "hello-recipient"))
        ) {
            os.write("Hello from INBOX!".getBytes());
        }

        long szInb = defaultDatasafeServices.inboxService().list(ListRequest.forDefaultPrivate(user, "./")).count();
        System.out.println("User has " + szInb + " files in INBOX");

        System.out.println(new String(
                ByteStreams.toByteArray(
                        defaultDatasafeServices.inboxService().read(ReadRequest.forDefaultPrivate(user, "hello-recipient"))
                )
        ));
    }
}
