package de.adorsys.datasafe.business.impl.dfscredentialservice;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.datasafe.business.api.dfscredentialservice.DFSCredentialService;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Disabled // FIXME !!!
public class DFSCredentialServiceTest {

    public static final String DFS_FS_PATH = "./target/filesystemstorage"; //default path for FS DFS implementation
    public static final String SYSTEM_DFS_DIRECTORY_NAME = "system-dfs";
    private DFSCredentialService dfsCredentialService = new DFSCredentialServiceImpl();

    private DFSCredentials dfsCredentialsExpected = new DFSCredentials();

    @BeforeEach
    public void setUp() {
        dfsCredentialsExpected.setS3accessKey("s3AccessKey");
        dfsCredentialsExpected.setSecret("secret");
    }

    @Test
    public void testSuccessPathWhenRegisterDFS() {
        UserIDAuth userIDAuth = getTestUser();

        dfsCredentialService.registerDFS(dfsCredentialsExpected, userIDAuth);

        DFSCredentials dfsCredentialsActual = dfsCredentialService.getDFSCredentials(userIDAuth);

        Assertions.assertEquals(dfsCredentialsExpected.getS3accessKey(), dfsCredentialsActual.getS3accessKey());
        Assertions.assertEquals(dfsCredentialsExpected.getSecret(), dfsCredentialsActual.getSecret());
    }

    @Test
    public void testDFSCredentialsSaved() {
        UserIDAuth userIDAuth = new UserIDAuth();
        userIDAuth.setReadKeyPassword(new ReadKeyPassword("pass"));
        UserID testUserID = new UserID("userID");
        userIDAuth.setUserID(testUserID);

        dfsCredentialService.registerDFS(dfsCredentialsExpected, userIDAuth);

        Assertions.assertTrue(new File(
          DFS_FS_PATH + BucketPath.BUCKET_SEPARATOR +
                    SYSTEM_DFS_DIRECTORY_NAME + BucketPath.BUCKET_SEPARATOR +
                    testUserID.getValue()
           ).exists()
        );
    }

    @Test
    public void testAlreadyRegisteredUser() {
        UserIDAuth userIDAuth = getTestUser();

        dfsCredentialService.registerDFS(dfsCredentialsExpected, userIDAuth);

        Assertions.assertThrows(DFSCredentialException.class,
                () -> dfsCredentialService.registerDFS(dfsCredentialsExpected, userIDAuth));
    }

    private UserIDAuth getTestUser() {
        UserIDAuth userIDAuth = new UserIDAuth();
        userIDAuth.setReadKeyPassword(new ReadKeyPassword("pass"));
        userIDAuth.setUserID(new UserID("testUserID"));
        return userIDAuth;
    }

    @AfterEach
    public void cleanUp() throws IOException {
        Files.walk(Paths.get(DFS_FS_PATH))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
