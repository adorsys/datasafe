package de.adorsys.datasafe.business.impl.inbox.impl;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.deployment.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.datasafe.business.impl.BaseMockitoTest;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDocusafeService;
import de.adorsys.datasafe.business.impl.service.DefaultDocusafeService;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static de.adorsys.datasafe.business.impl.profile.DFSSystem.CREDS_ID;

class InboxServiceImplDaggerTest extends BaseMockitoTest {

    private DefaultDocusafeService docusafeService = DaggerDefaultDocusafeService
            .builder()
            .build();

    @Test
    void testDaggerObjectCreation(@TempDir Path dfsLocation) {
        System.setProperty("SC-FILESYSTEM", dfsLocation.toFile().getAbsolutePath());

        UserIDAuth john = registerUser("John");
        UserIDAuth jane = registerUser("Jane");

        docusafeService.inboxService().list(john);
    }

    private UserIDAuth registerUser(String userName) {
        UserIDAuth auth = new UserIDAuth();
        auth.setUserID(new UserID(userName));
        auth.setReadKeyPassword(new ReadKeyPassword("secure-password " + userName));

        docusafeService.userProfile().registerPublic(CreateUserPublicProfile.builder()
            .id(auth.getUserID())
            .inbox(access(new BucketPath("inbox")))
            .publicKeys(access(new BucketPath("public")))
            .build()
        );

        docusafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .privateStorage(access(new BucketPath("private")))
            .keystore(access(new BucketPath("keystore")))
            .build()
        );


        return auth;
    }

    private DFSAccess access(BucketPath path) {
        return DFSAccess.builder()
            .physicalPath(path)
            .logicalPath(path)
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
    }
}
