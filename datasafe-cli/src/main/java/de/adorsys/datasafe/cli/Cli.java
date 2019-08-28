package de.adorsys.datasafe.cli;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import dagger.Lazy;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.keys.StorageKeyStoreOperations;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.directory.impl.profile.config.DFSConfigWithStorageCreds;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImpl;
import de.adorsys.datasafe.directory.impl.profile.dfs.BucketAccessServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.directory.impl.profile.dfs.RegexAccessServiceWithStorageCredentialsImpl;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.RegexDelegatingStorage;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.api.UriBasedAuthStorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3ClientFactory;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.resource.*;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.regex.Pattern;

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
        DefaultDatasafeServices datasafe = datasafeServices(root, "PAZZWORT");

        UserIDAuth user = new UserIDAuth("me", "mememe");
        UserIDAuth userRecipient = new UserIDAuth("recipient", "RRRE!!");

        datasafe.userProfile().registerUsingDefaults(user);
        datasafe.userProfile().registerUsingDefaults(userRecipient);

        datasafe.userProfile().registerStorageCredentials(user, StorageIdentifier.DEFAULT, awsCredentials());
        datasafe.userProfile().registerStorageCredentials(userRecipient, StorageIdentifier.DEFAULT, awsCredentials());

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
        OverridesRegistry registry = new BaseOverridesRegistry();
        DefaultDatasafeServices multiDfsDatasafe = DaggerDefaultDatasafeServices
                .builder()
                .config(new DataOnS3(fsRoot.toUri().toASCIIString(), systemPassword))
                .storage(
                        new RegexDelegatingStorage(
                                ImmutableMap.<Pattern, StorageService>builder()
                                        .put(Pattern.compile("file:/.+"), localFs(fsRoot))
                                        .put(Pattern.compile("s3://.+"), amazonS3()).build()
                        )
                )
                .overridesRegistry(registry)
                .build();

        BucketAccessServiceImplRuntimeDelegatable.overrideWith(
                registry, args -> new WithCredentialProvider(args.getStorageKeyStoreOperations())
        );

        return multiDfsDatasafe;
    }

    private static StorageCredentials awsCredentials() {
        return new StorageCredentials(
                System.getenv("AWS_ACCESS_KEY"),
                System.getenv("AWS_SECRET_KEY")
        );
    }

    private static StorageService localFs(Path fsRoot) {
        return new FileSystemStorageService(fsRoot);
    }

    private static StorageService amazonS3() {
        return new UriBasedAuthStorageService(
                acc -> new S3StorageService(
                        S3ClientFactory.getClientByRegion(
                                acc.getOnlyHostPart().toString().split("://")[1],
                                acc.getAccessKey(),
                                acc.getSecretKey()
                        ),
                        // Bucket name is encoded in first path segment
                        acc.getBucketName(),
                        ExecutorServiceUtil.submitterExecutesOnStarvationExecutingService()
                )
        );
    }

    private static class WithCredentialProvider extends BucketAccessServiceImpl {

        @Delegate
        private final RegexAccessServiceWithStorageCredentialsImpl delegate;

        private WithCredentialProvider(Lazy<StorageKeyStoreOperations> storageKeyStoreOperations) {
            super(null);
            this.delegate = new RegexAccessServiceWithStorageCredentialsImpl(storageKeyStoreOperations);
        }
    }

    private static class DataOnS3 extends DFSConfigWithStorageCreds {

        private DataOnS3(String systemRoot, String systemPassword) {
            super(systemRoot, systemPassword);
        }

        @Override
        public CreateUserPublicProfile defaultPublicTemplate(UserID id) {
            return super.defaultPublicTemplate(id);
        }

        @Override
        public CreateUserPrivateProfile defaultPrivateTemplate(UserIDAuth id) {
            return super.defaultPrivateTemplate(id).toBuilder()
                    .privateStorage(BasePrivateResource.forAbsolutePrivate("s3://eu-central-1/adorsys-docusafe/"))
                    .build();
        }
    }
}
