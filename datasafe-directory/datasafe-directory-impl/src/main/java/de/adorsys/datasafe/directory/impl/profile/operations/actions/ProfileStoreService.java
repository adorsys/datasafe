package de.adorsys.datasafe.directory.impl.profile.operations.actions;

import de.adorsys.datasafe.directory.api.config.DFSConfig;
import de.adorsys.datasafe.directory.api.profile.dfs.BucketAccessService;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.UserPublicProfile;
import de.adorsys.datasafe.directory.impl.profile.operations.UserProfileCache;
import de.adorsys.datasafe.directory.impl.profile.serde.GsonSerde;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.OutputStream;

@Slf4j
@RuntimeDelegate
public class ProfileStoreService {

    private final GsonSerde serde;
    private final UserProfileCache profileCache;
    private final DFSConfig dfsConfig;
    private final BucketAccessService access;
    private final StorageWriteService writeService;

    @Inject
    public ProfileStoreService(GsonSerde serde, UserProfileCache profileCache, DFSConfig dfsConfig,
                               BucketAccessService access, StorageWriteService writeService) {
        this.serde = serde;
        this.profileCache = profileCache;
        this.dfsConfig = dfsConfig;
        this.access = access;
        this.writeService = writeService;
    }

    @SneakyThrows
    public void registerPrivate(UserID id, UserPrivateProfile profile) {
        log.debug("Register private {}", profile);
        try (OutputStream os = writeService.write(
            WithCallback.noCallback(access.withSystemAccess(dfsConfig.privateProfile(id))))
        ) {
            os.write(serde.toJson(profile).getBytes());
        }
        profileCache.getPrivateProfile().remove(id);
    }

    @SneakyThrows
    public void registerPublic(UserID id, UserPublicProfile profile) {
        log.debug("Register public {}", profile);
        try (OutputStream os = writeService.write(
            WithCallback.noCallback(access.withSystemAccess(dfsConfig.publicProfile(id))))
        ) {
            os.write(serde.toJson(profile).getBytes());
        }
        profileCache.getPublicProfile().remove(id);
    }
}
