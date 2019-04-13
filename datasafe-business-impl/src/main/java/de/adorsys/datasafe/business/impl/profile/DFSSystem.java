package de.adorsys.datasafe.business.impl.profile;

import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;

import javax.inject.Inject;

public class DFSSystem {

    private static final String PATH = "/system";

    public static final String CREDS_ID = "SYS-001";

    @Inject
    public DFSSystem() {
    }

    public DFSAccess systemDfs() {

        return DFSAccess.builder()
            .logicalPath(new BucketPath(PATH))
            .physicalPath(new BucketPath(PATH))
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
    }
}
